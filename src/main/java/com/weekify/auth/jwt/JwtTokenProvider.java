package com.weekify.auth.jwt;

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

    public JwtToken createToken(Long userId){
        String accessToken = createAccessToken(userId);

        return JwtToken.of(
                accessToken,
                getAccessTokenExpirationSeconds()
        );
    }

    // JWT 생성용이 아닌 응답 DTO를 채우기 위한 용도의 메서드
    public long getAccessTokenExpirationSeconds(){
        return jwtProperties.accessTokenExpiration() / 1000;
    }

}
