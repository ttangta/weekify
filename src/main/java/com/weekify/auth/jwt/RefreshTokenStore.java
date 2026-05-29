package com.weekify.auth.jwt;
// Redis에 refreshToken의 jti를 저장하고, 재발급 시 기존 refreshToken을 한 번만 사용 가능하게 만드는 클래스
// 즉, 해당 클래스는 refreshToken의 유효 상태를 Redis에서 관리하는 저장소 역할

import com.weekify.auth.exception.ReusedRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    /*
     * save()
     * - 새로 발급한 refreshToken의 jti를 Redis에 저장한다.
     * - 로그인 성공, 회원가입 성공, 토큰 재발급 성공 시 호출된다.
     * - TTL은 refreshToken 만료 시간과 동일하게 설정한다.
     */

    public void save(String jti, Long userId, long refreshTokenExpirationMillis){
        redisTemplate.opsForValue().set(
                key(jti),
                String.valueOf(userId),
                Duration.ofMillis(refreshTokenExpirationMillis)
        );
    }

    /*
     * consume()
     * - 재발급 요청에 사용된 기존 refreshToken의 jti를 Redis에서 조회한다.
     * - 조회와 동시에 삭제한다.
     * - 이미 사용된 refreshToken이면 Redis에 값이 없으므로 예외를 던진다
     * - refreshToken rotation의 핵심 메서드
     */
    public void consume(String jti, Long userId){
        String savedUserId = redisTemplate.opsForValue().getAndDelete(key(jti));

        /*
         * 1번째 조건: savedUserId == null
         * - 해당 jti가 Redis에 없다는 뜻인데, 이미 한 번 사용되어 삭제됐거나 TTL이 만료된 상황
         *   즉, 이미 쓴 토큰을 다시 쓰려는 시도이므로 탈취 가능성으로 판단
         *
         * 2번째 조건: !savedUserId.equals(String.valueOf(userId))
         * - Redis에 저장된 userId와 토큰에서 꺼낸 userId가 다르다는 뜻 -> 누군가 다른 사용자의 jti를 조작해서 요청한 상황
         */
        if(savedUserId == null || !savedUserId.equals(String.valueOf(userId))){
            throw new ReusedRefreshTokenException();
        }
    }

    /*
     * delete()
     * - 특정 refreshToken jti를 강제로 삭제한다
     * - 추후 로그아웃 기능에서 사용
     */
    private void delete(String jti){
        redisTemplate.delete(key(jti));
    }

    /*
     * key()
     * - Redis key 포멧을 통일한다.
     * ex : jti = abc-123 -> refresh:abc-123
     */
    private String key(String jti){
        return KEY_PREFIX + jti;
    }
}
