package com.weekify.auth.exception;

import com.weekify.common.exception.BusinessException;
import com.weekify.common.exception.ErrorCode;

// 회원가입 시 이메일 중복 상황을 표현하는 구체적인 예외
public class DuplicatedEmailException extends BusinessException {

    public DuplicatedEmailException(){
        // 409 CONFLICT + DUPLICATED_EMAIL + 기본 메시지 보유
        super(ErrorCode.DUPLICATED_EMAIL);
    }
}
