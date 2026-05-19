package com.weekify.user.repository;

import com.weekify.user.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    // save(userCredential) : 회원가입 시 해싱된 비밀번호 저장

    // 이후 로그인/계정 검증 시 사용자 비밀번호 해시 조회
    Optional<UserCredential> findByUserId(Long userId);
}
