

package com.gaocui.modules.auth.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.config.properties.GaocuiProperties;
import com.gaocui.modules.auth.entity.VerificationCode;
import com.gaocui.modules.auth.mapper.VerificationCodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 验证码服务.
 * 开发期(devPrint=true)验证码仅控制台打印, 不真实发邮件; 后期接入邮件即可切换.
 */
@Service
public class VerifyCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerifyCodeService.class);

    private final VerificationCodeMapper verificationCodeMapper;
    private final GaocuiProperties props;

    public VerifyCodeService(VerificationCodeMapper verificationCodeMapper, GaocuiProperties props) {
        this.verificationCodeMapper = verificationCodeMapper;
        this.props = props;
    }

    /** 生成并保存验证码. 开发模式下打印到控制台. */
    public void sendCode(String email, String scene) {
        String code = RandomUtil.randomNumbers(props.getVerifyCode().getLength());
        VerificationCode vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(code);
        vc.setScene(scene);
        vc.setUsed(0);
        vc.setExpireTime(LocalDateTime.now().plusSeconds(props.getVerifyCode().getExpire()));
        verificationCodeMapper.insert(vc);

        if (props.getVerifyCode().isDevPrint()) {
            // 控制台醒目打印, 便于开发期联调
            System.out.println("\n╔══════════ 邮箱验证码 ══════════╗");
            System.out.println("║  邮箱: " + email);
            System.out.println("║  验证码: " + code + "  (有效期 " + props.getVerifyCode().getExpire() + "s)");
            System.out.println("╚════════════════════════════════╝\n");
        }
        log.info("[验证码] 已为 {} 生成(scene={})", email, scene);
    }

    /** 校验验证码(匹配且未使用且未过期), 通过后置为已使用. */
    public boolean verify(String email, String code, String scene) {
        VerificationCode vc = verificationCodeMapper.selectOne(new LambdaQueryWrapper<VerificationCode>()
                .eq(VerificationCode::getEmail, email)
                .eq(VerificationCode::getCode, code)
                .eq(VerificationCode::getScene, scene)
                .eq(VerificationCode::getUsed, 0)
                .gt(VerificationCode::getExpireTime, LocalDateTime.now())
                .orderByDesc(VerificationCode::getId)
                .last("LIMIT 1"));
        if (vc == null) {
            return false;
        }
        vc.setUsed(1);
        verificationCodeMapper.updateById(vc);
        return true;
    }
}
