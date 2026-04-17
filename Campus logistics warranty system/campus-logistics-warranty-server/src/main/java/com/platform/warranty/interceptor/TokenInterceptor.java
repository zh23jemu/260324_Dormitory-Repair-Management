package com.platform.warranty.interceptor;

import com.platform.warranty.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public static final String BEARER = "Bearer ";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/upload")){
            return true;
        }

        String header = request.getHeader("Authorization");
        String token = Optional.ofNullable(header)
                .filter(e -> e.startsWith(BEARER))
                .map(e -> e.replaceFirst(BEARER, "")).orElse(null);
        Long validUid = jwtUtils.getUidOrNull(token);
        if (Objects.nonNull(validUid)) {
            request.setAttribute("uid", validUid);
            return true;
        } else {
            response.setStatus(401);
            return false;
        }
    }
}