package com.weekify.auth.jwt;

import com.weekify.auth.dto.RefreshTokenClaims;
import com.weekify.auth.exception.InvalidRefreshTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {
    private static final String secret = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";
    private static final long accessTokenExpiration = 3600000L;
    private static final long refreshTokenExpiration = 1209600000L;
    @Test @DisplayName("Access Token과 Refresh Token 생성 + JWT 형식 검증 + 두 값이 서로 다른지 검증 + 각 JWT의 payload 검증")
    void jwtTokenClaimsCheck() {
        JwtProperties jwtProperties =
                new JwtProperties(secret, accessTokenExpiration, refreshTokenExpiration);


        JwtTokenProvider jwtTokenProvider =
                new JwtTokenProvider(jwtProperties);

        Long userId = 1L;

        JwtToken jwtToken = jwtTokenProvider.createToken(userId);

        // 1. access Token 과 refresh Token이 생성됨을 검증
        assertThat(jwtToken.accessToken()).isNotBlank();
        assertThat(jwtToken.refreshToken()).isNotBlank();
        // 2. access Token/refresh Token 모두 JWT 형식인지 검증
        assertThat(jwtToken.accessToken().split("\\.")).hasSize(3);
        assertThat(jwtToken.refreshToken().split("\\.")).hasSize(3);
        // 3. access Token/refresh Token이 같지 않음을 검증
        assertThat(jwtToken.accessToken())
                .isNotEqualTo(jwtToken.refreshToken());


        // 5. accessToken 과 refreshToken의 Claims를 확인
        SecretKey secretKey =
                Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // 5-1 accessToken의 Claims
        Claims accessClaims =
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(jwtToken.accessToken())
                        .getPayload();
        assertThat(accessClaims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(accessClaims.get("type", String.class))
                .isEqualTo("ACCESS");
        assertThat(accessClaims.getIssuedAt()).isNotNull();
        assertThat(accessClaims.getExpiration()).isNotNull();
        assertThat(accessClaims.getExpiration()).isAfter(accessClaims.getIssuedAt());

        // accessToken 생성 시 설정된 만료시간과 실제 accessToken의 만료시간 - 생성 시간의 값이 같은지 검증
        long accessTokenExpirationSeconds =
                (accessClaims.getExpiration().getTime() -
                        accessClaims.getIssuedAt().getTime()) / 1000;
        //assertThat(accessTokenExpirationSeconds).isEqualTo(accessTokenExpiration/1000);
        // JwtTokenProvider 내부에서 issuedAt과 expiration을 각각 다른 시점에 생성하면 드물게 1초 차이가 발생 가능성 존재
        // 좀 더 안정적으로 하려면 +=1초 범위로 검증을 수행
        assertThat(accessTokenExpirationSeconds)
                .isBetween(
                        accessTokenExpiration / 1000 -1,
                        accessTokenExpiration / 1000 +1
                );

        // 5-2 refreshToken의 Claims
        Claims refreshClaims =
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(jwtToken.refreshToken())
                        .getPayload();
        assertThat(refreshClaims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(refreshClaims.get("type", String.class))
                .isEqualTo("REFRESH");
        assertThat(refreshClaims.getIssuedAt()).isNotNull();
        assertThat(refreshClaims.getExpiration()).isNotNull();
        assertThat(refreshClaims.getExpiration()).isAfter(refreshClaims.getIssuedAt());

        // refreshToken 생성 시 설정된 만료시간과 실제 refreshToken의 만료시가 - 생성 시간의 값이 같은지 검증
        long refreshTokenExpirationSeconds =
                (refreshClaims.getExpiration().getTime() -
                        refreshClaims.getIssuedAt().getTime()) / 1000;
        //assertThat(refreshTokenExpirationSeconds).isEqualTo(refreshTokenExpiration/1000);
        assertThat(refreshTokenExpirationSeconds)
                .isBetween(
                        refreshTokenExpiration / 1000 - 1,
                        refreshTokenExpiration / 1000 + 1
                );
    }

    @Test @DisplayName("refreshToken을 파싱하면 userId와 jti를 추출한다.")
    void parseRefreshToken(){
        // given
        JwtProperties jwtProperties =
                new JwtProperties(secret, accessTokenExpiration, refreshTokenExpiration);

        JwtTokenProvider jwtTokenProvider =
                new JwtTokenProvider(jwtProperties);

        Long userId = 1L;
        JwtToken jwtToken = jwtTokenProvider.createToken(userId);

        // when
        RefreshTokenClaims claims =
                jwtTokenProvider.parseRefreshToken(jwtToken.refreshToken());

        // then
        assertThat(claims.userId()).isEqualTo(userId);
        assertThat(claims.jti()).isNotBlank();
    }

    @Test @DisplayName("accessToken을 refreshToken으로 파싱하면 예외가 발생한다.")
    void parseRefreshTokenWithAccessToken(){
        JwtProperties jwtProperties =
                new JwtProperties(secret, accessTokenExpiration, refreshTokenExpiration);

        JwtTokenProvider jwtTokenProvider =
                new JwtTokenProvider(jwtProperties);

        Long userId = 1L;
        JwtToken jwtToken = jwtTokenProvider.createToken(userId);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.parseRefreshToken(jwtToken.accessToken()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
