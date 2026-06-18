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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客资服务: 游客留资、商家客资列表/详情、联系状态、免费邮箱脱敏.
 */
@Service
public class LeadService {

    private final LeadMapper leadMapper;
    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantService merchantService;
    private final NotificationService notificationService;

    public LeadService(LeadMapper leadMapper, ProductMapper productMapper,
                       MerchantMapper merchantMapper, MerchantService merchantService,
                       NotificationService notificationService) {
        this.leadMapper = leadMapper;
        this.productMapper = productMapper;
        this.merchantMapper = merchantMapper;
        this.merchantService = merchantService;
        this.notificationService = notificationService;
    }

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
            vo.setBuyerEmail(vip ? l.getBuyerEmail() : maskEmail(l.getBuyerEmail()));
            vo.setMessage(l.getMessage());
            vo.setStatus(l.getStatus());
            vo.setCreatedTime(l.getCreatedTime());
            return vo;
        }).toList();
        return PageResult.of(result, records);
    }

    // ==================== 客资详情 ====================
    public LeadDetailVO detail(Long id) {
        Lead l = mustGetOwned(id);
        boolean vip = isCurrentVip();
        Product product = productMapper.selectById(l.getProductId());
        Merchant merchant = merchantMapper.selectById(l.getMerchantId());

        LeadDetailVO vo = new LeadDetailVO();
        vo.setId(l.getId());
        vo.setProductId(l.getProductId());
        vo.setProductTitle(product == null ? null : product.getTitle());
        vo.setMessage(l.getMessage());
        vo.setBuyerEmail(vip ? l.getBuyerEmail() : maskEmail(l.getBuyerEmail()));
        vo.setMerchantEmail(merchant == null ? null : merchant.getEmail());
        vo.setStatus(l.getStatus());
        vo.setCreatedTime(l.getCreatedTime());
        return vo;
    }

    // ==================== 标记已联系 ====================
    public void markContacted(Long id) {
        Lead l = mustGetOwned(id);
        l.setStatus(Lead.STATUS_CONTACTED);
        leadMapper.updateById(l);
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

    private Map<Long, String> loadProductTitles(List<Lead> leads) {
        Set<Long> ids = leads.stream().map(Lead::getProductId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, Product::getTitle, (a, b) -> a));
    }

    /**
     * 邮箱脱敏: 按 PRD "除@外均显示*".
     * 例: buyer1@email.com -> ******@*****.***
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        return email.replaceAll("[^@]", "*");
    }
}
