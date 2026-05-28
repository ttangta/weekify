package com.weekify.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    public JwtTokenProvider(JwtProperties jwtProperties){
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }
    // accessToken 생성 메서드
    private String createAccessToken(Long userId){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    // refreshToken 생성 메서드
    private String createRefreshToken(Long userId){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // 재발급 API를 만들려면 refreshToken에서 userId를 꺼내야 함
    public Long getUserId(String token){
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }
    // 만료/위조 토큰이면 JJWT 예외 발생 -> 해당 예외를 잡아서 INVALID_TOKEN 같은 에러로 변환하는 처리는 추후 진행

    public JwtToken createToken(Long userId){
        String accessToken = createAccessToken(userId);
        String refreshToken = createRefreshToken(userId);

        return JwtToken.of(
                accessToken,
                refreshToken,
                getAccessTokenExpirationSeconds()
        );
    }

    // JWT 생성용이 아닌 응답 DTO를 채우기 위한 용도의 메서드
    public long getAccessTokenExpirationSeconds(){
        return jwtProperties.accessTokenExpiration() / 1000;
    }

}
