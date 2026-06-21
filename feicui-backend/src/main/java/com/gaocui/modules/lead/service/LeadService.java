package com.gaocui.modules.lead.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaocui.common.api.PageResult;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.exception.BusinessException;
import com.gaocui.common.security.SecurityContext;
import com.gaocui.modules.lead.dto.LeadDetailVO;
import com.gaocui.modules.lead.dto.LeadListItemVO;
import com.gaocui.modules.lead.dto.LeadSubmitRequest;
import com.gaocui.modules.lead.entity.Lead;
import com.gaocui.modules.lead.mapper.LeadMapper;
import com.gaocui.modules.merchant.entity.Merchant;
import com.gaocui.modules.merchant.mapper.MerchantMapper;
import com.gaocui.modules.merchant.service.MerchantService;
import com.gaocui.modules.notify.service.NotificationService;
import com.gaocui.modules.product.entity.Product;
import com.gaocui.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客资服务: 游客留资、商家客资列表/详情、联系状态、联系方式可见性分层.
 * VIP 全部客资可见完整邮箱; 免费商家: 已联系(CONTACTED)全部完整 + 未联系(PENDING)最近 3 条完整, 其余脱敏(本地名前 3 位 + 完整域名).
 */
@RequiredArgsConstructor
@Service
public class LeadService {

    private final LeadMapper leadMapper;
    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantService merchantService;
    private final NotificationService notificationService;

    // ==================== 游客留资 ====================
    public Long submit(Long productId, LeadSubmitRequest req) {
        Product product = productMapper.selectById(productId);
        if (product == null || !Product.STATUS_LISTED.equals(product.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        Lead lead = new Lead();
        lead.setProductId(productId);
        lead.setMerchantId(product.getMerchantId());
        lead.setBuyerEmail(req.getBuyerEmail());
        lead.setMessage(req.getMessage());
        lead.setStatus(Lead.STATUS_PENDING);
        leadMapper.insert(lead);

        // 生成站内通知(webNotify 默认开启且不可关闭); 邮件提醒按 emailNotify 设置, 开发期暂不发送
        notificationService.notifyNewLead(product.getMerchantId(), product.getTitle());
        return lead.getId();
    }

    // ==================== 商家客资列表(按状态过滤) ====================
    public PageResult<LeadListItemVO> page(long current, long size, String status) {
        Long merchantId = SecurityContext.currentMerchantId();
        boolean vip = isCurrentVip();
        Set<Long> fullIds = fullEmailLeadIds(vip, merchantId);

        Page<Lead> page = new Page<>(current, size);
        LambdaQueryWrapper<Lead> qw = new LambdaQueryWrapper<Lead>()
                .eq(Lead::getMerchantId, merchantId)
                .eq(status != null && !status.isBlank(), Lead::getStatus, status)
                .orderByDesc(Lead::getCreatedTime);
        Page<Lead> result = leadMapper.selectPage(page, qw);

        // 批量查关联商品标题
        Map<Long, String> titleMap = loadProductTitles(result.getRecords());
        List<LeadListItemVO> records = result.getRecords().stream().map(l -> {
            LeadListItemVO vo = new LeadListItemVO();
            vo.setId(l.getId());
            vo.setProductId(l.getProductId());
            vo.setProductTitle(titleMap.get(l.getProductId()));
            boolean showFull = vip || Lead.STATUS_CONTACTED.equals(l.getStatus()) || fullIds.contains(l.getId());
            vo.setBuyerEmail(showFull ? l.getBuyerEmail() : maskEmail(l.getBuyerEmail()));
            vo.setMessage(l.getMessage());
            vo.setStatus(l.getStatus());
            vo.setCreatedTime(l.getCreatedTime());
            return vo;
        }).toList();
        return PageResult.of(result, records);
    }

    // ==================== 首页最近客资预览(邮箱完整) ====================
    public List<LeadListItemVO> recent(int limit) {
        int safe = Math.max(1, Math.min(limit, 10));
        Long merchantId = SecurityContext.currentMerchantId();

        Page<Lead> page = new Page<>(1, safe);
        LambdaQueryWrapper<Lead> qw = new LambdaQueryWrapper<Lead>()
                .eq(Lead::getMerchantId, merchantId)
                .orderByDesc(Lead::getCreatedTime);
        Page<Lead> result = leadMapper.selectPage(page, qw);

        Map<Long, String> titleMap = loadProductTitles(result.getRecords());
        return result.getRecords().stream().map(l -> {
            LeadListItemVO vo = new LeadListItemVO();
            vo.setId(l.getId());
            vo.setProductId(l.getProductId());
            vo.setProductTitle(titleMap.get(l.getProductId()));
            vo.setBuyerEmail(l.getBuyerEmail()); // 首页预览特权: 完整邮箱, 不脱敏
            vo.setMessage(l.getMessage());
            vo.setStatus(l.getStatus());
            vo.setCreatedTime(l.getCreatedTime());
            return vo;
        }).toList();
    }

    // ==================== 客资详情 ====================
    public LeadDetailVO detail(Long id) {
        Lead l = mustGetOwned(id);
        boolean vip = isCurrentVip();
        Set<Long> fullIds = fullEmailLeadIds(vip, l.getMerchantId());
        Product product = productMapper.selectById(l.getProductId());
        Merchant merchant = merchantMapper.selectById(l.getMerchantId());

        LeadDetailVO vo = new LeadDetailVO();
        vo.setId(l.getId());
        vo.setProductId(l.getProductId());
        vo.setProductTitle(product == null ? null : product.getTitle());
        vo.setMessage(l.getMessage());
        boolean showFull = vip || Lead.STATUS_CONTACTED.equals(l.getStatus()) || fullIds.contains(l.getId());
        vo.setBuyerEmail(showFull ? l.getBuyerEmail() : maskEmail(l.getBuyerEmail()));
        vo.setEmailFullVisible(showFull);
        vo.setMerchantEmail(merchant == null ? null : merchant.getEmail());
        vo.setStatus(l.getStatus());
        vo.setCreatedTime(l.getCreatedTime());
        return vo;
    }

    // ==================== 标记已联系 ====================
    public void markContacted(Long id) {
        Lead l = mustGetOwned(id);
        // 防绕过付费墙: 对当前商家脱敏的客资不允许标记, 否则标记成 CONTACTED 后即完整可见
        if (!canSeeFullEmail(l)) {
            throw new BusinessException(ResultCode.LEAD_EMAIL_MASKED);
        }
        l.setStatus(Lead.STATUS_CONTACTED);
        leadMapper.updateById(l);
    }

    /** 当前商家对该客资是否可见完整邮箱(标记前校验, 防绕过付费墙) */
    private boolean canSeeFullEmail(Lead l) {
        if (isCurrentVip() || Lead.STATUS_CONTACTED.equals(l.getStatus())) {
            return true;
        }
        return fullEmailLeadIds(false, l.getMerchantId()).contains(l.getId());
    }

    // ==================== 内部工具 ====================

    private Lead mustGetOwned(Long id) {
        Lead l = leadMapper.selectById(id);
        if (l == null || !l.getMerchantId().equals(SecurityContext.currentMerchantId())) {
            throw new BusinessException(ResultCode.LEAD_NOT_FOUND);
        }
        return l;
    }

    /** 当前商家是否 VIP(按生效层级) */
    private boolean isCurrentVip() {
        Merchant m = merchantService.getCurrent();
        return Merchant.TIER_VIP.equals(merchantService.effectiveTier(m));
    }

    /**
     * 免费商家可看完整邮箱的"未联系"客资 id 集合(按留资时间倒序最近 3 条).
     * VIP 返回 null, 语义为"全部完整"; 已联系(CONTACTED)客资另行按状态判为完整, 不在此集合.
     * 列表与详情共用, 保证分页下单条判定一致.
     */
    private Set<Long> fullEmailLeadIds(boolean vip, Long merchantId) {
        if (vip) {
            return null;
        }
        List<Lead> top = leadMapper.selectList(new LambdaQueryWrapper<Lead>()
                .select(Lead::getId)
                .eq(Lead::getMerchantId, merchantId)
                .eq(Lead::getStatus, Lead.STATUS_PENDING)
                .orderByDesc(Lead::getCreatedTime)
                .last("LIMIT 3"));
        return top.stream().map(Lead::getId).collect(Collectors.toSet());
    }

    private Map<Long, String> loadProductTitles(List<Lead> leads) {
        Set<Long> ids = leads.stream().map(Lead::getProductId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, Product::getTitle, (a, b) -> a));
    }

    /**
     * 邮箱脱敏: 本地名保留前 3 字符, 其余打码; @ 与域名完整保留.
     * 本地名不足 3 位时原样返回(信息量太少, 脱敏无意义).
     * 例: zhangsan@126.com -> zha****@126.com ; mik**@gmail.com ; ab@163.com -> ab@163.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return email; // 非标准邮箱, 不脱敏
        }
        String local = email.substring(0, at);
        String domain = email.substring(at); // 含 @
        if (local.length() <= 3) {
            return email;
        }
        StringBuilder sb = new StringBuilder(local.length() + domain.length());
        sb.append(local, 0, 3);
        for (int i = 3; i < local.length(); i++) {
            sb.append('*');
        }
        sb.append(domain);
        return sb.toString();
    }
}
