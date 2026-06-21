package com.gaocui.modules.notify.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统通知列表项.
 */
@Getter
@Setter
public class NotificationVO {

    private Long id;
    private String type;
    private String content;
    private Integer isRead;
    private LocalDateTime createdTime;
}
