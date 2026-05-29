package com.weekify.auth.controller;

import com.weekify.auth.dto.*;
import com.weekify.auth.service.AuthService;
import com.weekify.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-api/auth")
public class OpenApiAuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "일반 회원가입을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequest request
    ){
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 엑세스 토큰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 검증 실패 또는 잘못된 요청 본문"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ){
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }


    /*
     * AuthController.reissue()
     * - POST /open-api/auth/reissue 요청을 받는다.
     * - 요청 바디를 TokenReissueRequest로 변환한다.
     * - @Valid로 refreshToken 누락/빈 문자열을 검증한다.
     * - 실제 재발급 로직은 AuthService.reissue()에 위임한다.
     * - 성공 시 200 OK와 TokenReissueResponse를 변환한다.
     */
    @PostMapping("/reissue")
    public ResponseEntity<TokenReissueResponse> reissue(
            @Valid @RequestBody TokenReissueRequest request
    ){
        return ResponseEntity.ok(authService.reissue(request));
    }
}
