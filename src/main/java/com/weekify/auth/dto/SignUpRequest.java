package com.weekify.auth.dto;

import com.weekify.common.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignUpRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @ValidPassword
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
        String name,

        @NotBlank(message = "전화번호는 필수입니다.")
        String tel,

        // 2026-05-21 리뷰 오타 수정
        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @Schema(
                description = "주소",
                example = "서울시 강남구",
                nullable = false,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "주소는 필수입니다.")
        String address,

        String profileImageUrl
) {
}
