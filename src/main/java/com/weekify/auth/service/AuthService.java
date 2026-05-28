package com.weekify.auth.service;

import com.weekify.auth.dto.*;
import com.weekify.auth.exception.DuplicatedEmailException;
import com.weekify.auth.exception.LoginFailedException;
import com.weekify.auth.jwt.JwtToken;
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

        return LoginResponse.of(
                jwtToken,
                UserSummaryResponse.from(user)
        );
    }

}
