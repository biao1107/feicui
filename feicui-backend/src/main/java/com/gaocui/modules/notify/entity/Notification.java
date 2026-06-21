package com.gaocui.modules.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gaocui.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统通知实体.
 * type: NEW_LEAD 新客资 / VIP_EXPIRE VIP到期提醒.
 */
@Getter
@Setter
@TableName("t_notification")
public class Notification extends BaseEntity {

    public static final String TYPE_NEW_LEAD = "NEW_LEAD";
    public static final String TYPE_VIP_EXPIRE = "VIP_EXPIRE";

    private Long merchantId;
    private String type;
    private String content;
    /** 是否已读: 0 否 1 是 */
    private Integer isRead;
}
