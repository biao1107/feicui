package com.gaocui.modules.merchant.controller;

import com.gaocui.common.api.Result;
import com.gaocui.modules.merchant.dto.DashboardVO;
import com.gaocui.modules.merchant.dto.MerchantProfileVO;
import com.gaocui.modules.merchant.dto.NotificationSettingRequest;
import com.gaocui.modules.merchant.dto.UpdateEmailRequest;
import com.gaocui.modules.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家后台接口 (需登录, /merchant/** 由 AuthInterceptor 拦截).
 */
@Tag(name = "商家", description = "资料/面板/通知设置等")
@RequiredArgsConstructor
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "获取商家资料(个人中心)")
    @GetMapping("/profile")
    public Result<MerchantProfileVO> profile() {
        return Result.success(merchantService.getProfile());
    }

    @Operation(summary = "商家后台数据面板")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(merchantService.dashboard());
    }

    @Operation(summary = "修改邮箱")
    @PutMapping("/email")
    public Result<Void> updateEmail(@RequestBody @Valid UpdateEmailRequest req) {
        merchantService.updateEmail(req);
        return Result.success();
    }

    @Operation(summary = "更新通知设置")
    @PutMapping("/notification-settings")
    public Result<Void> updateNotificationSettings(@RequestBody @Valid NotificationSettingRequest req) {
        merchantService.updateNotificationSettings(req);
        return Result.success();
    }

    @Operation(summary = "升级/续费VIP(模拟开通, 演示用)")
    @PostMapping("/upgrade")
    public Result<MerchantProfileVO> upgrade(@RequestParam(defaultValue = "12") int months) {
        return Result.success(merchantService.upgrade(months));
    }

    @Operation(summary = "退出登录(客户端清除 token 即可)")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
