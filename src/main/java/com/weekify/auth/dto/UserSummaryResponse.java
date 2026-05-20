package com.weekify.auth.dto;

public record UserSummaryResponse(
        Long id,
        String email,
        String name
) {
}
