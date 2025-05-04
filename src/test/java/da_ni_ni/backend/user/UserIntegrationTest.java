package da_ni_ni.backend.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import da_ni_ni.backend.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.secret=TestSecretKey12345678901234567890",
                "jwt.expiration-ms=3600000",
                "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
// 각 테스트 메소드가 끝날 때마다 스프링 컨텍스트(=인메모리 DB)를 리셋합니다.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate rt = new RestTemplate();

    String baseUrl;
    SignupRequestDto signupDto;
    LoginRequestDto loginDto;
    EmailCheckRequestDto emailCheckDto;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/users";

        signupDto = new SignupRequestDto();
        signupDto.setName("테스트");
        signupDto.setEmail("test@b.com");
        signupDto.setPassword("pw");

        loginDto = new LoginRequestDto();
        loginDto.setEmail(signupDto.getEmail());
        loginDto.setPassword(signupDto.getPassword());

        emailCheckDto = new EmailCheckRequestDto();
    }

    @Test
    void signupAndLoginFlow() {
        // --- 회원가입 성공
        var signupRes =
                rt.postForEntity(baseUrl + "/signup", signupDto, Void.class);
        assertThat(signupRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // --- 로그인 성공
        var loginRes =
                rt.postForEntity(baseUrl + "/login", loginDto, LoginResponseDto.class);
        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        var body = loginRes.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getEmail()).isEqualTo("test@b.com");
        assertThat(body.getName()).isEqualTo("테스트");
        assertThat(body.getToken()).isNotBlank();
    }

    @Test
    void signupFailWhenDuplicateEmail() throws JsonProcessingException {
        String url = baseUrl + "/signup";

        // 첫 번째 가입은 OK
        var first = rt.postForEntity(url, signupDto, Void.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 두 번째 가입 시도는 409 Conflict 예외
        HttpClientErrorException.Conflict e = assertThrows(
                HttpClientErrorException.Conflict.class,
                () -> rt.postForEntity(url, signupDto, Void.class)
        );
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // 응답 JSON 파싱 및 검증
        ObjectMapper om = new ObjectMapper();
        ErrorResponseDto err = om.readValue(e.getResponseBodyAsString(), ErrorResponseDto.class);
        assertThat(err.getStatus()).isEqualTo(409);
        assertThat(err.getMessage()).isEqualTo("이미 존재하는 이메일입니다.");
    }

    @Test
    void loginFailWhenWrongCredentials() throws JsonProcessingException {
        // given: 정상 가입
        rt.postForEntity(baseUrl + "/signup", signupDto, Void.class);

        // when: 잘못된 비밀번호로 로그인 시도
        LoginRequestDto wrong = new LoginRequestDto();
        wrong.setEmail(signupDto.getEmail());
        wrong.setPassword("wrongpw");

        // then: 400 Bad Request 에러 던져지는지 검사
        HttpClientErrorException e = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> rt.postForEntity(baseUrl + "/login", wrong, Void.class)
        );

        // 에러 바디 파싱 및 검증
        ObjectMapper om = new ObjectMapper();
        ErrorResponseDto err = om.readValue(e.getResponseBodyAsString(), ErrorResponseDto.class);
        assertThat(err.getStatus()).isEqualTo(400);
        assertThat(err.getMessage()).isEqualTo("잘못된 이메일 또는 비밀번호입니다.");
    }

    @Test
    void checkEmailWithoutAuth() {
        // 가입되지 않은 이메일
        emailCheckDto.setEmail("noone@b.com");
        var res1 =
                rt.postForEntity(baseUrl + "/check-email", emailCheckDto, EmailCheckResponseDto.class);
        assertThat(res1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res1.getBody()).isNotNull();
        assertThat(res1.getBody().isDuplicated()).isFalse();  // :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}

        // 가입된 이메일
        signupDto.setEmail("exists@b.com");
        rt.postForEntity(baseUrl + "/signup", signupDto, Void.class);

        emailCheckDto.setEmail("exists@b.com");
        var res2 =
                rt.postForEntity(baseUrl + "/check-email", emailCheckDto, EmailCheckResponseDto.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res2.getBody()).isNotNull();
        assertThat(res2.getBody().isDuplicated()).isTrue();  // :contentReference[oaicite:2]{index=2}:contentReference[oaicite:3]{index=3}
    }
}
