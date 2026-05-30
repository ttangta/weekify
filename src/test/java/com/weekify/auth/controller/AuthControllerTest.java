package com.weekify.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weekify.auth.dto.*;
import com.weekify.auth.service.AuthService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/*
@WebMvcTest로 컨트롤러 계층만 테스트하려고 했는데, 메인 클래스에 붙어 있던
@EnableJpaAuditing 때문에 JPA auditing 설정까지 같이 로드 => 실패 jpaAuditingHandler, jpaMappingContext, JPA metamodel must not be empty

slice test를 사용할 때 특정 영역에 필요한 설정을 메인 애플리케이션 클래스에 직접 추가는 피해야함
@WebMvcTest 같은 slice test는 웹 계층 테스트에 필요한 일부 구성만 로드
 */
@WebMvcTest(OpenApiAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String LOGIN_URI = "/open-api/auth//login";
    private static final String SIGN_UP_URI = "/open-api/auth//signup";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test @DisplayName("로그인 성공 응답에 tokenType 필드는 포함되지 않는다.")
    void login_success_response_not_include_tokenType() throws Exception{
        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "password123!"
        );

        UserSummaryResponse user = new UserSummaryResponse(
                7L,
                "test@example.com",
                "홍길동"
        );

        LoginResponse response = new LoginResponse(
                "access-token",
                "refresh-token",
                user
        );

        given(authService.login(any(LoginRequest.class)))
                .willReturn(response);

        // when  & then
        mockMvc.perform(post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.tokenType").doesNotExist());

    }

    @Test @DisplayName("회원가입 성공 응답에 tokenType 필드는 포함되지 않는다.")
    void signUp_success_response_not_include_tokenType() throws Exception{
        // given
        SignUpRequest request = new SignUpRequest(
                "test@example.com",
                "password123!",
                "홍길동",
                "010-1234-5678",
                LocalDate.of(1991,1,1),
                "서울시 강남구",
                null
        );

        UserSummaryResponse user = new UserSummaryResponse(
                7L,
                "test@example.com",
                "홍길동"
        );

        SignUpResponse response = new SignUpResponse(
                "access-token",
                "refresh-token",
                user
        );

        given(authService.signUp(any(SignUpRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post(SIGN_UP_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.tokenType").doesNotExist());
    }

    @Test @DisplayName("토큰 재발급 성공 시 accessToken, refreshToken, expiresIn을 반환한다")
    void reissue_success() throws Exception{
        // given
        TokenReissueRequest request = new TokenReissueRequest("refresh-token");

        TokenReissueResponse response = new TokenReissueResponse(
            "new-access-token",
            "new-refresh-token"
        );
        given(authService.reissue(any(TokenReissueRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/open-api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test @DisplayName("refreshToken 필드가 누락되면 400을 반환한다")
    void reissue_missing_refreshToken() throws Exception{
        mockMvc.perform(post("/open-api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("refreshToken"));
    }

    @Test @DisplayName("refreshToken이 빈 문자열이면 400을 반환한다")
    void reissue_blank_refreshToken() throws Exception{
        mockMvc.perform(post("/open-api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "refreshToken": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("refreshToken"));
    }

    @Test @DisplayName("refreshToken이 공백 문자열이면 400을 반환한다")
    void reissue_whitespace_refreshToken() throws Exception{
        mockMvc.perform(post("/open-api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "refreshToken": "   "
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("refreshToken"));
    }



}
