package com.gaocui.common.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaocui.common.api.Result;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器: 校验 Authorization 头中的 JWT, 通过则写入 {@link SecurityContext}.
 * <p>失败时直接写出 401 标准响应.</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非 Controller 方法(静态资源等)直接放行
        String header = request.getHeader(jwtUtils.getHeader());
        if (header == null || !header.startsWith(jwtUtils.getTokenPrefix())) {
            return writeUnauthorized(response);
        }
        String token = header.substring(jwtUtils.getTokenPrefix().length()).trim();
        try {
            DecodedJWT jwt = jwtUtils.verify(token);
            Long merchantId = Long.valueOf(jwt.getSubject());
            String email = jwt.getClaim("email").asString();
            String tier = jwt.getClaim("tier").asString();
            SecurityContext.set(new LoginUser(merchantId, email, tier));
            return true;
        } catch (JWTVerificationException e) {
            return writeUnauthorized(response);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理, 避免线程池复用导致登录态串号
        SecurityContext.clear();
    }

    private boolean writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(ResultCode.UNAUTHORIZED)));
        return false;
    }
}
