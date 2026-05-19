package com.weekify.user.entity;

import com.weekify.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String tel;

    private LocalDate birthDate;

    @Column(length = 255)
    private String address;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserCredential userCredential;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserOAuthAccount> oauthAccounts = new ArrayList<>();

    // 일반 회원가입용 User 생성 메서드.
    // setter를 열지 않고 생성 규칙을 한 곳에서 관리하기 위해 정적 팩토리 메서드를 사용
    // @Builder 대신 명시적인 생성 메서드를 사용해 필수값 누락과 의도하지 않은 객체 생성 방지
    public static User createLocalUser(
            String email,
            String name,
            String tel,
            LocalDate birthDate,
            String address,
            String profileImageUrl
    ){
        User user = new User();
        user.email = email;
        user.name = name;
        user.tel = tel;
        user.birthDate = birthDate;
        user.address = address;
        user.profileImageUrl = profileImageUrl;
        user.isActive = true;
        return user;
    }
}
