package com.weekify.auth.dto;

import com.weekify.auth.jwt.JwtToken;
import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpResponse(
        @Schema(
                description = "엑세스 토큰. Authorization 헤더에 Bearer {accessToken} 형식으로 사용합니다."
        )
        String accessToken,

        @Schema(description = "리프레시 토큰. 엑세스 토큰 재발급 시 사용합니다.")
        String refreshToken,

        @Schema(
                description = "엑세스 토큰 만료 시간(초)", example = "3600"
        )
        long expiresIn,

        @Schema(description = "회원가입 사용자 정보")
        UserSummaryResponse user
) {
    public static SignUpResponse of(
            JwtToken jwtToken,
            UserSummaryResponse user
    ){
        return new SignUpResponse(
                jwtToken.accessToken(),
                jwtToken.refreshToken(),
                jwtToken.expiresIn(),
                user
        );
    }
}