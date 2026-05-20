package com.weekify.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secret;
    private final long accessTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration
    ){
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    // Access Token을 생성하는 메서드
    public String createAccessToken(Long userId, String email){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    // JWT 생성용이 아닌 응답 DTO를 채우기 위한 용도의 메서드
    public long getAccessTokenExpirationSeconds(){
        return accessTokenExpiration / 1000;
    }

}
