package com.weekify.auth.service;

import com.weekify.auth.dto.RefreshTokenClaims;
import com.weekify.auth.dto.TokenReissueRequest;
import com.weekify.auth.dto.TokenReissueResponse;
import com.weekify.auth.exception.LoginFailedException;
import com.weekify.auth.exception.ReusedRefreshTokenException;
import com.weekify.auth.jwt.JwtToken;
import com.weekify.auth.jwt.JwtTokenProvider;
import com.weekify.auth.jwt.RefreshTokenStore;
import com.weekify.user.entity.User;
import com.weekify.user.repository.UserCredentialRepository;
import com.weekify.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// 1. 정상 refreshToken으로 재발급 성공
// 2. 기존 refreshToken을 consume
// 3. 새 refreshToken을 save
// 4. 사용자가 없으면 LoginFailedException
// 5. Redis에 jti가 없으면 ReusedRefreshTokenException

/*
AuthServiceTest는 서비스 계층 단위 테스트 -> 실제 DB, Redis, JWT 생성 로직을 직접 돌리는게 아님
AuthService가 의존하는 객체들을 Mock으로 만들고 해당 의존성이 지정한 값을 반환하다고 가정했을 때 AuthService가 올바른 흐름으로 동작하는지를 검증

AuthService#reissue() 내부 흐름
JwtTokenProvider : refreshToken 파싱, 새 토큰 생성

RefreshTokenStore : 기존 refreshToken consume/delete, 새 refreshToken save

UserRepository : userId로 사용자 조회

 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @InjectMocks
    private AuthService authService;

    @Test @DisplayName("정상 refreshToken으로 새 accessToken과 refreshToken을 재발급한다.")
    void reissue_success(){
        // given
        Long userId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String oldJti = "old-jti";
        String newJti = "new-jti";
        long refreshTokenExpiration = 1209600000L;

        TokenReissueRequest request = new TokenReissueRequest(oldRefreshToken);

        RefreshTokenClaims oldClaims = new RefreshTokenClaims(userId, oldJti);

        User user = createTestUser(userId);

        JwtToken newJwtToken = new JwtToken(
                "new-access-token",
                "new-refresh-token",
                3600
        );

        RefreshTokenClaims newClaims = new RefreshTokenClaims(userId, newJti);

        // 기존 refreshToken을 파싱하면 userId와 oldJti가 추출된다고 가정
        given(jwtTokenProvider.parseRefreshToken(oldRefreshToken))
                .willReturn(oldClaims);

        // oldClaims에서 추출한 userId에 해당하는 사용자가 존재한다고 가정
        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        // 사용자 ID로 새 accessToken + refreshToken을 발급한다고 가정
        given(jwtTokenProvider.createToken(userId))
                .willReturn(newJwtToken);

        // 새 refreshToken을 파싱하면 newJti가 추출된다고 가정
        given(jwtTokenProvider.parseRefreshToken(newJwtToken.refreshToken()))
                .willReturn(newClaims);

        // Redis 저장 TTL로 사용할 refreshToken 만료 시간을 반환한다고 가정
        given(jwtTokenProvider.getRefreshTokenExpiration())
                .willReturn(refreshTokenExpiration);

        // when
        TokenReissueResponse response = authService.reissue(request);

        // then
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.expiresIn()).isEqualTo(3600);

        verify(refreshTokenStore).consume(oldJti, userId);
        verify(refreshTokenStore).save(newJti, userId, refreshTokenExpiration);
    }

    @Test @DisplayName("refreshToken의 userId에 해당하는 사용자가 없으면 예외가 발생한다")
    void reissue_userNotFound(){
        // given
        Long userId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String oldJti = "old-jti";

        TokenReissueRequest request = new TokenReissueRequest(oldRefreshToken);
        RefreshTokenClaims oldClaims = new RefreshTokenClaims(userId, oldJti);

        // 기존 refreshToken을 파싱하면 userId와 oldJti가 추출된다고 가정
        given(jwtTokenProvider.parseRefreshToken(oldRefreshToken))
                .willReturn(oldClaims);

        // 해당 userId를 가진 사용자가 DB에 없다고 가정
        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(request))
                .isInstanceOf(LoginFailedException.class);

        verify(refreshTokenStore).consume(oldJti, userId);
        verify(userRepository).findById(userId);
        verify(jwtTokenProvider, never()).createToken(anyLong());
        verify(refreshTokenStore, never()).save(anyString(), anyLong(), anyLong());
    }

    @Test @DisplayName("이미 사용된 refreshToken이면 예외가 발생한다")
    void reissue_reusedRefreshToken(){
        // given
        Long userId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String oldJti = "old-jti";

        TokenReissueRequest request = new TokenReissueRequest(oldRefreshToken);
        RefreshTokenClaims oldClaims = new RefreshTokenClaims(userId, oldJti);

        // 기존 refreshToken을 파싱하면 userId와 oldJti가 추출된다고 가정
        given(jwtTokenProvider.parseRefreshToken(oldRefreshToken))
                .willReturn(oldClaims);

        // Redis에 oldJti가 없거나 이미 소비된 refreshToken이라 consume 시 예외가 발생한다고 가정
        willThrow(new ReusedRefreshTokenException())
                .given(refreshTokenStore)
                .consume(oldJti, userId);

        // when & then
        assertThatThrownBy(() -> authService.reissue(request))
                .isInstanceOf(ReusedRefreshTokenException.class);

        verify(refreshTokenStore).consume(oldJti, userId);
        verify(userRepository, never()).findById(anyLong());
        verify(jwtTokenProvider, never()).createToken(anyLong());
        verify(refreshTokenStore, never()).save(anyString(), anyLong(), anyLong());
    }

    private User createTestUser(Long userId){
        User user = User.createLocalUser(
          "test@example.com",
          "홍길동",
          "010-1234-5678",
          LocalDate.of(1991,1,1),
          "서울시 강남구",
          null
        );
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }
}
