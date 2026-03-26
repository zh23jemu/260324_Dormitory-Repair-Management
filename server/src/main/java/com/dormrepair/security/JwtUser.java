package com.dormrepair.security;

public record JwtUser(Long id, String username, String role, String realName) {
}
