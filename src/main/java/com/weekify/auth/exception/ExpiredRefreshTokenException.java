package com.weekify.auth.exception;

import com.weekify.common.exception.BaseException;

public class ExpiredRefreshTokenException extends BaseException {
    public ExpiredRefreshTokenException(){
        super(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }
}
