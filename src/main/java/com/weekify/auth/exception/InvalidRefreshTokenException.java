package com.weekify.auth.exception;

import com.weekify.common.exception.BaseException;

public class InvalidRefreshTokenException extends BaseException {
    public InvalidRefreshTokenException(){
        super(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
