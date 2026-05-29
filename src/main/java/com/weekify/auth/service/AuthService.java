package com.weekify.auth.service;

import com.weekify.auth.dto.*;
import com.weekify.auth.exception.DuplicatedEmailException;
import com.weekify.auth.exception.LoginFailedException;
import com.weekify.auth.jwt.JwtToken;
import com.weekify.auth.jwt.JwtTokenProvider;
import com.weekify.auth.jwt.RefreshTokenStore;
import com.weekify.user.entity.User;
import com.weekify.user.entity.UserCredential;
import com.weekify.user.repository.UserCredentialRepository;
import com.weekify.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    // 회원가입 과정에서 실행되는 여러 DB 작업이 하나의 트랜잭션으로 묶임
    // User 저장 성공 -> UserCredential 저장 성공 -> commit
    // User 저장 성공 -> UserCredential 저장 실패 -> rollback -> User 저장도 취소
    // 즉, 하나의 메서드 내에서 DB 작업을 전부 성공하거나 전부 실패하는 하나의 작업 단위로 보장할 수 있게 함
    @Transactional
    public SignUpResponse signUp(SignUpRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new DuplicatedEmailException();
        }
        String passwordHash = passwordEncoder.encode(request.password());

        // User 객체 생성
        User user = User.createLocalUser(
                request.email(),
                request.name(),
                request.tel(),
                request.birthDate(),
                request.address(),
                request.profileImageUrl()
        );
        // User 저장
        User savedUser = userRepository.save(user);

        // 일반 로그인 인증 정보 생성 및 저장
        UserCredential userCredential = UserCredential.create(savedUser, passwordHash);
        userCredentialRepository.save(userCredential);

        JwtToken jwtToken = jwtTokenProvider.createToken(savedUser.getId());

        // 회원가입 성공 -> accessToken + refreshToken 발급 -> refreshToken의 jti를 Redis에 저장 -> 클라이언트에 토큰 응답
        saveRefreshToken(savedUser.getId(), jwtToken);

        // 회원가입 성공 응답 반환
        return SignUpResponse.of(
                jwtToken,
                UserSummaryResponse.from(savedUser)
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(LoginFailedException::new);

        UserCredential userCredential = userCredentialRepository.findByUserId(user.getId())
                .orElseThrow(LoginFailedException::new);

        if(!passwordEncoder.matches(request.password(), userCredential.getPasswordHash())){
            throw new LoginFailedException();
        }

        JwtToken jwtToken = jwtTokenProvider.createToken(user.getId());

        // 로그인 성공 -> accessToken + refreshToken 발급 -> refreshToken의 jti를 Redis에 저장 -> 클라이언트에 토큰 응답
        saveRefreshToken(user.getId(), jwtToken);

        return LoginResponse.of(
                jwtToken,
                UserSummaryResponse.from(user)
        );
    }

    /*
     * reissue()
     * - 클라이언트가 보낸 refreshToken을 파싱한다.
     * - type=REFRESH인지 검증한다 (JwtTokenProvider#parseRefreshToken 내 validateTokenType() 메서드를 통해 검증)
     * - sub에서 userId를 추출한다.
     * - jti를 추출한다.
     * - Redis에서 기존 jti를 조회하고 삭제한다.
     * - userId로 사용자를 조회한다.
     * - 새 accessToken + 새 refreshToken을 발급한다.
     * - 새 refreshToken의 jti를 Redis에 저장한다.
     * - 새 토큰들을 응답 DTO로 변환한다.
     */
    @Transactional
    public TokenReissueResponse reissue(TokenReissueRequest request){
        RefreshTokenClaims oldClaims =
                jwtTokenProvider.parseRefreshToken(request.refreshToken());

        refreshTokenStore.consume(oldClaims.jti(), oldClaims.userId());

        User user = userRepository.findById(oldClaims.userId())
                .orElseThrow(LoginFailedException::new);

        JwtToken newJwtToken = jwtTokenProvider.createToken(user.getId());

        saveRefreshToken(user.getId(), newJwtToken);

        return TokenReissueResponse.of(newJwtToken);
    }

    /*
     * savedRefreshToken()
     * - 발급된 refreshToken을 파싱한다.
     * - refreshToken 내부의 jti를 꺼낸다.
     * - Redis에 refresh:{jti} 형태로 저장한다.
     * - value에는 userId를 저장한다.
     * - TTL은 refreshToken 만료 시간으로 설정한다.
     */
    private void saveRefreshToken(Long userId, JwtToken jwtToken){
        RefreshTokenClaims refreshClaims =
                jwtTokenProvider.parseRefreshToken(jwtToken.refreshToken());

        refreshTokenStore.save(
                refreshClaims.jti(),
                userId,
                jwtTokenProvider.getRefreshTokenExpiration()
        );
    }
}
