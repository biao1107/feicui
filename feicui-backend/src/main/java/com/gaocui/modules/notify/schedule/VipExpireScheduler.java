package com.gaocui.modules.notify.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.config.properties.GaocuiProperties;
import com.gaocui.modules.merchant.entity.Merchant;
import com.gaocui.modules.merchant.mapper.MerchantMapper;
import com.gaocui.modules.notify.entity.Notification;
import com.gaocui.modules.notify.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * VIP 到期提醒定时任务.
 * 每天 09:00 扫描 30 天内即将到期的 VIP 商家, 生成站内提醒(每天每商家最多1条).
 */
@Component
public class VipExpireScheduler {

    private static final Logger log = LoggerFactory.getLogger(VipExpireScheduler.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MerchantMapper merchantMapper;
    private final NotificationService notificationService;
    private final GaocuiProperties props;

    public VipExpireScheduler(MerchantMapper merchantMapper, NotificationService notificationService, GaocuiProperties props) {
        this.merchantMapper = merchantMapper;
        this.notificationService = notificationService;
        this.props = props;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkVipExpiry() {
        int days = props.getMerchant().getVipNoticeDays();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);

        List<Merchant> expiring = merchantMapper.selectList(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getTier, Merchant.TIER_VIP)
                .isNotNull(Merchant::getVipExpireTime)
                .ge(Merchant::getVipExpireTime, now)
                .le(Merchant::getVipExpireTime, threshold));

        log.info("[VIP到期检查] {} 位商家即将到期", expiring.size());
        for (Merchant m : expiring) {
            // 去重: 今日已提醒则跳过
            if (notificationService.existsToday(m.getId(), Notification.TYPE_VIP_EXPIRE)) {
                continue;
            }
            String dateText = m.getVipExpireTime().toLocalDate().format(DATE_FMT);
            notificationService.notifyVipExpire(m.getId(), dateText);
        }
    }
}
