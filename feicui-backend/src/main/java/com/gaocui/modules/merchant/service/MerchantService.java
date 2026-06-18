package com.gaocui.modules.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.config.properties.GaocuiProperties;
import com.gaocui.common.exception.BusinessException;
import com.gaocui.common.security.SecurityContext;
import com.gaocui.modules.lead.entity.Lead;
import com.gaocui.modules.lead.mapper.LeadMapper;
import com.gaocui.modules.merchant.dto.DashboardVO;
import com.gaocui.modules.merchant.dto.MerchantProfileVO;
import com.gaocui.modules.merchant.dto.NotificationSettingRequest;
import com.gaocui.modules.merchant.dto.UpdateEmailRequest;
import com.gaocui.modules.merchant.entity.Merchant;
import com.gaocui.modules.merchant.mapper.MerchantMapper;
import com.gaocui.modules.product.entity.Product;
import com.gaocui.modules.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商家服务: 资料查询、数据面板、修改邮箱、通知设置、VIP/免费分层逻辑.
 */
@Service
public class MerchantService {

    private final MerchantMapper merchantMapper;
    private final ProductMapper productMapper;
    private final LeadMapper leadMapper;
    private final GaocuiProperties props;

    public MerchantService(MerchantMapper merchantMapper, ProductMapper productMapper,
                           LeadMapper leadMapper, GaocuiProperties props) {
        this.merchantMapper = merchantMapper;
        this.productMapper = productMapper;
        this.leadMapper = leadMapper;
        this.props = props;
    }

    /** 获取当前登录商家(由 JWT 拦截器写入 SecurityContext) */
    public Merchant getCurrent() {
        Long id = SecurityContext.currentMerchantId();
        if (id == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Merchant m = merchantMapper.selectById(id);
        if (m == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }
        return m;
    }

    /**
     * 生效的会员层级: VIP 且未到期才算 VIP, 否则按 FREE.
     * (VIP 过期后自动降级展示)
     */
    public String effectiveTier(Merchant m) {
        if (Merchant.TIER_VIP.equals(m.getTier())
                && m.getVipExpireTime() != null
                && m.getVipExpireTime().isAfter(LocalDateTime.now())) {
            return Merchant.TIER_VIP;
        }
        return Merchant.TIER_FREE;
    }

    /** 当前层级对应的发布上限 */
    public int productLimitOf(String effectiveTier) {
        return Merchant.TIER_VIP.equals(effectiveTier)
                ? props.getMerchant().getVipProductLimit()
                : props.getMerchant().getFreeProductLimit();
    }

    /** 个人中心资料 */
    public MerchantProfileVO getProfile() {
        Merchant m = getCurrent();
        String tier = effectiveTier(m);
        MerchantProfileVO vo = new MerchantProfileVO();
        vo.setId(m.getId());
        vo.setEmail(m.getEmail());
        vo.setTier(tier);
        vo.setVipExpireTime(m.getVipExpireTime());
        vo.setWebNotify(m.getWebNotify());
        vo.setEmailNotify(m.getEmailNotify());
        vo.setProductLimit(productLimitOf(tier));
        return vo;
    }

    /** 数据面板: 已上架商品数/上限、今日客资、累计客资 */
    public DashboardVO dashboard() {
        Merchant m = getCurrent();
        Long id = m.getId();
        String tier = effectiveTier(m);

        Long listedCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, id)
                .eq(Product::getStatus, Product.STATUS_LISTED));
        Long totalLeads = leadMapper.selectCount(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getMerchantId, id));
        Long todayLeads = leadMapper.selectCount(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getMerchantId, id)
                .ge(Lead::getCreatedTime, LocalDate.now().atStartOfDay()));

        DashboardVO vo = new DashboardVO();
        vo.setListedCount(listedCount);
        vo.setProductLimit(productLimitOf(tier));
        vo.setTotalLeads(totalLeads);
        vo.setTodayLeads(todayLeads);
        return vo;
    }

    /**
     * 修改邮箱.
     * PRD: 当前阶段验证码校验可不做(随便改). 这里直接更新邮箱.
     */
    public void updateEmail(UpdateEmailRequest req) {
        Merchant m = getCurrent();
        m.setEmail(req.getEmail());
        merchantMapper.updateById(m);
    }

    /**
     * 更新通知设置.
     * webNotify(站内)按 PRD 默认勾选且不可取消, 这里强制为 1.
     */
    public void updateNotificationSettings(NotificationSettingRequest req) {
        Merchant m = getCurrent();
        m.setWebNotify(1); // 站内通知强制开启
        m.setEmailNotify(req.getEmailNotify() == null ? 1 : req.getEmailNotify());
        merchantMapper.updateById(m);
    }
}
