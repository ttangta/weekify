package com.weekify.auth.exception;

import com.weekify.common.exception.BaseException;

public class LoginFailedException extends BaseException {
    public LoginFailedException() {
        super(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
    }
}
