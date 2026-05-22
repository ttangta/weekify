package com.weekify.auth.dto;

import com.weekify.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "엑세스 토큰")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "엑세스 토큰 만료 시간(ms)", example = "3600000")
        long expiresIn,

        @Schema(description = "로그인 사용자 정보")
        UserSummaryResponse user
) {
    public static LoginResponse of(
            String accessToken,
            long expiresIn,
            UserSummaryResponse user
    ) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresIn,
                user);
    }
}