package com.gaocui.modules.notify.controller;

import com.gaocui.common.api.PageResult;
import com.gaocui.common.api.Result;
import com.gaocui.modules.notify.dto.NotificationVO;
import com.gaocui.modules.notify.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统通知接口 (需登录).
 */
@Tag(name = "系统通知", description = "站内通知列表/未读数/标记已读")
@RestController
@RequestMapping("/merchant/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "通知列表(分页)")
    @GetMapping
    public Result<PageResult<NotificationVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(notificationService.page(current, size));
    }

    @Operation(summary = "未读通知数")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(Map.of("count", notificationService.unreadCount()));
    }

    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }
}
