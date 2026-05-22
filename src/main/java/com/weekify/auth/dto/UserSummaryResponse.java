package com.weekify.auth.dto;

import com.weekify.user.entity.User;

public record UserSummaryResponse(
        Long id,
        String email,
        String name
) {
    public static UserSummaryResponse from(User user){
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}
