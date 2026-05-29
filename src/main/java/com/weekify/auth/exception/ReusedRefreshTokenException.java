package com.weekify.auth.exception;

import com.weekify.common.exception.BaseException;
// Redis에 해당 jti가 없을 때 발생
// 이미 사용되었거나 폐기된 refreshToken을 다시 사용한 경우를 표현
// 재발급 공격 또는 토큰 재사용 상황을 구분하기 위한 예외
public class ReusedRefreshTokenException extends BaseException {

    public ReusedRefreshTokenException(){
        super(AuthErrorCode.REUSED_REFRESH_TOKEN);
    }
}
