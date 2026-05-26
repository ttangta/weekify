package com.weekify.auth.jwt;

/*
exp : 만료 시각(timestamp)
- accessToken 내부 JWT payload claim
- 토큰 자체가 "언제 만료되는지" 나타내는 값
- 예 : 1779691846

expiresIn : 남은 유효 시간(duration)
- 응답 JSON 바깥 필드
- 클라이언트가 "몇 초 뒤 만료되는지" 참고하기 위한 값
- 예 : 3600
 */
public record JwtToken(
        String accessToken,
        long expiresIn
) {
    public static JwtToken of(String accessToken, long expiresIn){
        return new JwtToken(
                accessToken,
                expiresIn
        );
    }
}
