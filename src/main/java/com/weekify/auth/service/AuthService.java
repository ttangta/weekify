package com.weekify.auth.service;

import com.weekify.auth.dto.SignUpRequest;
import com.weekify.auth.dto.SignUpResponse;
import com.weekify.auth.dto.UserSummaryResponse;
import com.weekify.auth.jwt.JwtTokenProvider;
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

    // 회원가입 과정에서 실행되는 여러 DB 작업이 하나의 트랜잭션으로 묶임
    // User 저장 성공 -> UserCredential 저장 성공 -> commit
    // User 저장 성공 -> UserCredential 저장 실패 -> rollback -> User 저장도 취소
    // 즉, 하나의 메서드 내에서 DB 작업을 전부 성공하거나 전부 실패하는 하나의 작업 단위로 보장할 수 있게 함
    @Transactional
    public SignUpResponse signUp(SignUpRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
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

        // Jwt accessToken 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                savedUser.getId(),
                savedUser.getEmail()
        );

        // 회원가입 성공 응답 반환
        return SignUpResponse.of(accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                new UserSummaryResponse(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getName()
                )
        );
    }
}
