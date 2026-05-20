package com.weekify.common.exception;

import lombok.Getter;

// 서비스 로직에서 의도적으로 발생시키는 비즈니스 예외의 공통 부모
@Getter
public class BusinessException extends RuntimeException{

    // 어떤 에러인지에 대한 정책 정보
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public BusinessException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }
}
