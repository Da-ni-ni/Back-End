package da_ni_ni.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import da_ni_ni.backend.user.dto.EmailCheckRequestDto;
import da_ni_ni.backend.user.dto.LoginRequestDto;
import da_ni_ni.backend.user.dto.LoginResponseDto;
import da_ni_ni.backend.user.dto.SignupRequestDto;
import da_ni_ni.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // ← 이 한 줄만 추가하면 인증/CSRF 필터를 모두 비활성화
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    @Test
    @DisplayName("회원가입 성공 시 200")
    void signupSuccess() throws Exception {
        SignupRequestDto req = new SignupRequestDto();
        req.setName("홍길동");
        req.setEmail("a@b.com");
        req.setPassword("pw");

        willDoNothing().given(userService).signup(any(SignupRequestDto.class));

        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 성공 시 name, email, token, groupId 반환")
    void loginSuccess() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("a@b.com");
        req.setPassword("pw");

        LoginResponseDto fake = new LoginResponseDto(
                "홍길동", "a@b.com", "fake-jwt-token", null
        );
        given(userService.login(any(LoginRequestDto.class)))
                .willReturn(fake);

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("a@b.com"))
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.groupId").isEmpty());
    }

    @Test
    @DisplayName("이메일 중복확인 시 duplicated=true 반환")
    void checkEmail() throws Exception {
        EmailCheckRequestDto req = new EmailCheckRequestDto();
        req.setEmail("dup@b.com");

        given(userService.isEmailDuplicated("dup@b.com"))
                .willReturn(true);

        mockMvc.perform(post("/api/v1/users/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicated").value(true));
    }
}
