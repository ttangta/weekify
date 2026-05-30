package com.weekify.auth.exception;

import com.weekify.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    DUPLICATED_EMAIL(
            HttpStatus.CONFLICT,
            "DUPLICATED_EMAIL",
            "이미 가입된 이메일입니다."
    ),
    // 로그인 실패 시 이메일 존재 여부나 비밀번호 불일치 여부가 노출되지 않도록 INVALID_LOGIN_CREDENTIALS 하나로 통합해서 처리하는 현재 방식이 적절하다고 판단
    // "이메일 또는 비밀번호가 올바르지 않습니다." 메시지 유지 -> DTO validation 실페는 별도로 필드별 에러 메시지를 내려주는 방식으로 구분
    INVALID_LOGIN_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "INVALID_LOGIN_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "INVALID_REFRESH_TOKEN",
            "유효하지 않은 리프레시 토큰입니다."
    ),

    EXPIRED_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "EXPIRED_REFRESH_TOKEN",
            "만료된 리프레시 토큰입니다."
    ),

    REUSED_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "REUSED_REFRESH_TOKEN",
            "이미 사용된 리프레시 토큰입니다."
    )
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
