package com.gaocui.modules.auth.controller;

import com.gaocui.common.api.Result;
import com.gaocui.modules.auth.dto.LoginRequest;
import com.gaocui.modules.auth.dto.LoginResponse;
import com.gaocui.modules.auth.dto.SendCodeRequest;
import com.gaocui.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口 (游客可访问, 无需登录).
 */
@Tag(name = "鉴权", description = "邮箱验证码登录/注册")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "发送邮箱验证码(开发期控制台打印)")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody @Valid SendCodeRequest req) {
        authService.sendCode(req.getEmail());
        return Result.success();
    }

    @Operation(summary = "邮箱验证码登录/注册(二合一)")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return Result.success(authService.login(req));
    }
}
