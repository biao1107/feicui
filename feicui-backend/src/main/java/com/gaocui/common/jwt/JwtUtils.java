package com.gaocui.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gaocui.common.config.properties.GaocuiProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具 (基于 com.auth0:java-jwt).
 * <p>Token 载荷: subject=merchantId, claims: email / tier.</p>
 */
@Component
public class JwtUtils {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final GaocuiProperties.Jwt props;

    public JwtUtils(GaocuiProperties properties) {
        this.props = properties.getJwt();
        this.algorithm = Algorithm.HMAC256(this.props.getSecret());
        this.verifier = JWT.require(this.algorithm).build();
    }

    /** 生成 token */
    public String create(Long merchantId, String email, String tier) {
        long expireMs = props.getExpire() * 1000L;
        JWTCreator.Builder builder = JWT.create()
                .withSubject(String.valueOf(merchantId))
                .withClaim("email", email)
                .withClaim("tier", tier)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expireMs));
        return builder.sign(algorithm);
    }

    /** 校验并解析 token, 失败抛 JWTVerificationException */
    public DecodedJWT verify(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    public String getHeader() {
        return props.getHeader();
    }

    public String getTokenPrefix() {
        return props.getTokenPrefix();
    }
}
