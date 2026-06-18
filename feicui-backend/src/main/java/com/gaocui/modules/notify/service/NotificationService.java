package com.gaocui.modules.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaocui.common.api.PageResult;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.exception.BusinessException;
import com.gaocui.common.security.SecurityContext;
import com.gaocui.modules.notify.dto.NotificationVO;
import com.gaocui.modules.notify.entity.Notification;
import com.gaocui.modules.notify.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内通知服务: 列表、未读数、标记已读, 以及内部"新客资"通知生成.
 */
@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public PageResult<NotificationVO> page(long current, long size) {
        Long mid = SecurityContext.currentMerchantId();
        Page<Notification> page = new Page<>(current, size);
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getMerchantId, mid)
                .orderByDesc(Notification::getCreatedTime);
        Page<Notification> result = notificationMapper.selectPage(page, qw);
        List<NotificationVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result, records);
    }

    public Long unreadCount() {
        Long mid = SecurityContext.currentMerchantId();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getMerchantId, mid)
                .eq(Notification::getIsRead, 0));
    }

    public void markRead(Long id) {
        Notification n = mustGetOwned(id);
        n.setIsRead(1);
        notificationMapper.updateById(n);
    }

    public void markAllRead() {
        Long mid = SecurityContext.currentMerchantId();
        Notification entity = new Notification();
        entity.setIsRead(1);
        notificationMapper.update(entity, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getMerchantId, mid)
                .eq(Notification::getIsRead, 0));
    }

    /**
     * 内部调用: 有新客资时生成站内通知.
     * (PRD: 站内通知默认开启且不可关闭, 故直接生成)
     */
    public void notifyNewLead(Long merchantId, String productTitle) {
        Notification n = new Notification();
        n.setMerchantId(merchantId);
        n.setType(Notification.TYPE_NEW_LEAD);
        n.setContent("有新客户对「" + productTitle + "」感兴趣，请及时联系");
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    /** 内部调用: VIP 即将到期提醒(由定时任务触发, 已去重) */
    public void notifyVipExpire(Long merchantId, String expireDateText) {
        Notification n = new Notification();
        n.setMerchantId(merchantId);
        n.setType(Notification.TYPE_VIP_EXPIRE);
        n.setContent("您的 VIP 会员将于 " + expireDateText + " 到期，请及时续费");
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    /** 今日是否已生成某类型通知(去重用) */
    public boolean existsToday(Long merchantId, String type) {
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getMerchantId, merchantId)
                .eq(Notification::getType, type)
                .ge(Notification::getCreatedTime, java.time.LocalDate.now().atStartOfDay()));
        return count != null && count > 0;
    }

    private Notification mustGetOwned(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || !n.getMerchantId().equals(SecurityContext.currentMerchantId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        return n;
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setContent(n.getContent());
        vo.setIsRead(n.getIsRead());
        vo.setCreatedTime(n.getCreatedTime());
        return vo;
    }
}
