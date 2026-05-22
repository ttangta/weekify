package com.weekify.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum CommonErrorCode implements ErrorCode {
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "잘못된 요청입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
    ),

    UNKNOWN_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "UNKNOWN_ERROR",
            "알 수 없는 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonErrorCode(HttpStatus status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
