package com.weekify.auth.jwt;

import com.weekify.auth.dto.RefreshTokenClaims;
import com.weekify.auth.exception.ExpiredRefreshTokenException;
import com.weekify.auth.exception.InvalidRefreshTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    public JwtTokenProvider(JwtProperties jwtProperties){
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }
    // 로그인/회원가입/재발급 시 accessToken과 refreshToken을 함께 발급한다.
    public JwtToken createToken(Long userId){
        String accessToken = createAccessToken(userId);
        String refreshToken = createRefreshToken(userId);

        return JwtToken.of(
                accessToken,
                refreshToken
        );
    }

    // 인증 요청에 사용할 accessToken 생성 (만료 시간이 짧다. type=ACCESS claim을 가진다. jti는 필요하지 않다.)
    private String createAccessToken(Long userId){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    // accessToken 재발급에 사용할 refreshToken을 생성한다. (만료 시간이 accessToken보다 길다. type=REFRESH claim을 가진다. jti를 가진다. jti는 Redis 저장/검증에 사용)
    private String createRefreshToken(Long userId){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * refreshToken을 검증하고 userId, jti를 추출한다.
     *
     * 검증 내용:
     * - JWT 형식
     * - 서명
     * - 만료 시간
     * - type-REFRESH
     * - sub 존재 여부
     * - jti 존재 여부
     */
    public RefreshTokenClaims parseRefreshToken(String refreshToken){
        Claims claims = parseRefreshTokenClaims(refreshToken);

        validateTokenType(claims, REFRESH_TOKEN_TYPE);
        validateRefreshTokenClaims(claims);

        return new RefreshTokenClaims(
                Long.valueOf(claims.getSubject()),
                claims.getId()
        );
    }

    /**
     * refreshToken의 Claims를 파싱한다.
     *
     * 만료된 refreshToken이면 ExpiredRefreshTokenException,
     * 형식/서명 등이 잘못된 refreshToken이면 InValidRefreshTokenException을 던진다.
     */
    private Claims parseRefreshTokenClaims(String token){
        try{
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch(ExpiredJwtException e){
            throw new ExpiredRefreshTokenException();
        }catch(JwtException | IllegalArgumentException e){
            throw new InvalidRefreshTokenException();
        }
    }

    /**
     * 토큰 타입을 검증한다.
     *
     * ex
     * - refreshToken 검증 시 expectedType = REFRESH
     * - accessToken이 들어오면 type=ACCESS라서 실패
     */
    private void validateTokenType(Claims claims, String expectedType){
        String actualType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        if(!expectedType.equals(actualType)){
            throw new InvalidRefreshTokenException();
        }
    }

    /**
     * refreshToken에 필요한 필수 claims을 검증한다.
     *
     * refreshToken에는 반드시 sub와 jti가 있어야 한다.
     */
    private void validateRefreshTokenClaims(Claims claims){
        String subject = claims.getSubject();
        String jti = claims.getId();

        if(subject == null || subject.isBlank()){
            throw new InvalidRefreshTokenException();
        }
        if(jti == null || jti.isBlank()){
            throw new InvalidRefreshTokenException();
        }

        try{
            Long.valueOf(subject);
        }catch(NumberFormatException e){
            throw new InvalidRefreshTokenException();
        }
    }

    /**
     * Redis TTL 저장 시 사용할 refreshToken 만료 시간(ms)을 반환한다.
     */
    public Long getRefreshTokenExpiration(){
        return jwtProperties.refreshTokenExpiration();
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



    // JWT 생성용이 아닌 응답 DTO를 채우기 위한 용도의 메서드
    public long getAccessTokenExpirationSeconds(){
        return jwtProperties.accessTokenExpiration() / 1000;
    }

}
