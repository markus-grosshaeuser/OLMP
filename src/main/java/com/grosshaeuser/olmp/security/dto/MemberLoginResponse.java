package com.grosshaeuser.olmp.security.dto;

public record MemberLoginResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds
) {
}