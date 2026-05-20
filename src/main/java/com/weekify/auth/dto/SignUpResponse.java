package com.weekify.auth.dto;

public record SignUpResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummaryResponse user
) {
    public static SignUpResponse of(String accessToken,
                                    long expiresIn,
                                    UserSummaryResponse user){
        return new SignUpResponse(
                accessToken,
                "Bearer",
                expiresIn,
                user
        );
    }
}