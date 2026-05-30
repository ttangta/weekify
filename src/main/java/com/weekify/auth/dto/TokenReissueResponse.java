package com.weekify.auth.dto;

import com.weekify.auth.jwt.JwtToken;
import io.swagger.v3.oas.annotations.media.Schema;

// 재발급 성공 시 새 accessToken과 새 refreshToken을 응답으로 내려주기 위한 DTO
public record TokenReissueResponse(
        // 새로 발급된 accessToken
        @Schema(description = "새로 발급된 엑세스 토큰")
        String accessToken,

        // 새로 발급된 refreshToken, 기존 refreshToken은 재발급 과정에서 폐기
        @Schema(description = "새로 발급된 리프레시 토큰")
        String refreshToken

) {
    public static TokenReissueResponse of(JwtToken jwtToken){
        return new TokenReissueResponse(
                jwtToken.accessToken(),
                jwtToken.refreshToken()
        );
    }
}
