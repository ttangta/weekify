package com.weekify.user.entity;

import com.weekify.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_oauth_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOAuthAccount extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false, length = 255)
    private String providerId;
}
