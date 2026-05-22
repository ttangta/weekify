package com.weekify.auth.exception;

import com.weekify.common.exception.BusinessException;

public class LoginFailedException extends BusinessException {
    public LoginFailedException() {
        super(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
    }
}
