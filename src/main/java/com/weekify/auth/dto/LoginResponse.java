package com.weekify.auth.dto;

import com.weekify.auth.jwt.JwtToken;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "엑세스 토큰. Authorization 헤더에 Bearer {accessToken} 형식으로 사용합니다.")
        String accessToken,

        @Schema(description = "엑세스 토큰 만료 시간(초)", example = "3600")
        long expiresIn,

        @Schema(description = "로그인 사용자 정보")
        UserSummaryResponse user
) {
    public static LoginResponse of(
            JwtToken jwtToken,
            UserSummaryResponse user
    ) {
        return new LoginResponse(
                jwtToken.accessToken(),
                jwtToken.expiresIn(),
                user
        );
    }
}