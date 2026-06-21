package com.gaocui.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.exception.BusinessException;
import com.gaocui.common.jwt.JwtUtils;
import com.gaocui.modules.auth.dto.LoginRequest;
import com.gaocui.modules.auth.dto.LoginResponse;
import com.gaocui.modules.auth.entity.VerificationCode;
import com.gaocui.modules.merchant.entity.Merchant;
import com.gaocui.modules.merchant.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 鉴权服务: 发送验证码 + 邮箱验证码登录/注册(二合一).
 * 登录时若邮箱不存在则自动注册为 FREE 商家.
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final VerifyCodeService verifyCodeService;
    private final MerchantMapper merchantMapper;
    private final JwtUtils jwtUtils;

    /** 发送登录/注册验证码 */
    public void sendCode(String email) {
        verifyCodeService.sendCode(email, VerificationCode.SCENE_LOGIN);
    }

    /** 邮箱验证码登录/注册 */
    public LoginResponse login(LoginRequest req) {
        boolean ok = verifyCodeService.verify(req.getEmail(), req.getCode(), VerificationCode.SCENE_LOGIN);
        if (!ok) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }

        // 邮箱不存在则自动注册
        Merchant m = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getEmail, req.getEmail()));
        if (m == null) {
            m = new Merchant();
            m.setEmail(req.getEmail());
            m.setTier(Merchant.TIER_FREE);
            m.setWebNotify(1);
            m.setEmailNotify(1);
            merchantMapper.insert(m);
        }

        String token = jwtUtils.create(m.getId(), m.getEmail(), m.getTier());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setMerchantId(m.getId());
        resp.setEmail(m.getEmail());
        resp.setTier(m.getTier());
        resp.setVipExpireTime(m.getVipExpireTime());
        return resp;
    }
}
