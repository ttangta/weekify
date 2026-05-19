package com.weekify.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    DUPLICATION_EMAIL(HttpStatus.CONFLICT, "DUPLICATED_EMAIL", "이미 가입된 이메일입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생핬습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus(){
        return status;
    }

    public String getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}
