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

    INVALID_LOGIN_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "INVALID_LOGIN_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
