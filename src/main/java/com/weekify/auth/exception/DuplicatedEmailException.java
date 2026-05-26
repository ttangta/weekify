package com.weekify.auth.exception;

import com.weekify.common.exception.BaseException;

// 회원가입 시 이메일 중복 상황을 표현하는 구체적인 예외
public class DuplicatedEmailException extends BaseException {

    public DuplicatedEmailException(){
        // 409 CONFLICT + DUPLICATED_EMAIL + 기본 메시지 보유
        super(AuthErrorCode.DUPLICATED_EMAIL);
        //super(CommonErrorCode.DUPLICATED_EMAIL);
    }
}
