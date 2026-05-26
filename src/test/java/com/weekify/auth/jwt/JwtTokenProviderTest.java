package com.weekify.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {
    /*
    JWT 내부 payload에 exp claim이 들어가는지 확인한다.
    응답용 expiresIn과 JWT 내부 exp 시간이 3600초러 일치하는지 확인
     */
    @Test @DisplayName("발급된 JWT payload에 sub, iat, exp claim 포함 여부")
    void createToken_containsSubIatExpClaims(){
        /*
        JwtTokenProvider가 발급한 accessToken 내부 JWT payload에 sub(사용자 식별자), iat(발급 시각), exp(만료 시각) claim이 포함되는지 검증한다.
        또한 응답 편의용 expiresIn 값과 JWT 내부의 exp - iat 값이 설정한 만료 시각(3600초)과 일치하는지 확인한다.
         */
        String secret = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";
        long accessTokenExpiration = 3600000L;

        JwtProperties jwtProperties = new JwtProperties(
                secret,
                accessTokenExpiration
        );

        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        Long userId = 7L;

        // JwtTokenProvider가 토큰 발급에 사용한 곳과 동일한 secret 문자열로 테스트 코드에서 검증용 SecretKey를 다시 만드는 것
        // JwtTokenProvider가 accessToken 발급
        JwtToken jwtToken = jwtTokenProvider.createToken(userId);

        // 응답용 JwtToken 값 검증 : accessToken 생성 여부와 expiresIn 초 단위 변화 확인
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(jwtToken.expiresIn()).isEqualTo(3600L);

        // JwtTokenProvider가 토큰 발급에 사용한 것과 동일한 secret으로 검증용 SecretKey를 만들어, 발급된 accessToken을 파싱하고 exp claim을 확인하기 위해 사용
        // 테스트 코드에서 같은 secret으로 검증용 SecretKey를 만듬
        SecretKey secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        // 해당 Claims 객체는 JwtTokenProvider가 생성한 accessToken을 테스트 코드에서 만든 SecretKey로 파싱한 뒤, JWT payload 안의 claim 값을 확인하기 위한 객체
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken.accessToken())
                .getPayload();

        // JWT 표준 claim 검증 : sub는 사용자 id, iat는 발급 시각, exp는 만료 시각이다.
        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        // JWT 내부의 실제 유효 시간(exp - ita)이 설정한 만료 시간과 일치하는 검증한다.
        long expirationSeconds =
                (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertThat(expirationSeconds).isEqualTo(3600L);
    }
}
