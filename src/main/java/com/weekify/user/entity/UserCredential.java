package com.weekify.user.entity;

import com.weekify.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCredential extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    public static UserCredential create(User user, String passwordHash){
        UserCredential credential = new UserCredential();
        credential.user = user;
        credential.passwordHash = passwordHash;
        return credential;
    }
}
