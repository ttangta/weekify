package com.weekify.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
// 클라이언트가 재발급 API에 refreshToken을 보내기 위한 요청 DTO
public record TokenReissueRequest(
        // 클라이언트가 기존에 발급받은  refreshToken
        // null, 빈 문자열, 공백 문자열 요청 막기
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {
}
