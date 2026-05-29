package com.weekify.auth.dto;
// 검증된 refreshToken에서 꺼낸 핵심 정보를 담는다.
public record RefreshTokenClaims(
        // JWT subject(sub)에서 추출한 사용자 ID
        Long userId,
        // refreshToken의 고유 ID, Redis key로 사용
        String jti
) {
}
