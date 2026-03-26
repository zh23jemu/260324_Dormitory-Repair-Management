package com.dormrepair.util;

import com.dormrepair.common.BusinessException;
import com.dormrepair.security.JwtUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static JwtUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser user)) {
            throw new BusinessException("未登录或登录已失效");
        }
        return user;
    }

    public static void requireRole(String... roles) {
        String currentRole = currentUser().role();
        for (String role : roles) {
            if (role.equals(currentRole)) {
                return;
            }
        }
        throw new BusinessException("无权限访问");
    }
}
