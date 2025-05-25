package da_ni_ni.backend.refreshtoken;

import da_ni_ni.backend.user.jwt.JwtAuthenticationFilter;
import da_ni_ni.backend.user.jwt.JwtTokenProvider;
import da_ni_ni.backend.user.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 인증 필터 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);

        testUserDetails = new User(
                "test@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증 성공")
    void authenticateWithValidToken() throws ServletException, IOException {
        // given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;
        String userEmail = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmail(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(testUserDetails);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmail(validToken);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없는 경우")
    void noAuthorizationHeader() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 접두사가 없는 Authorization 헤더")
    void authorizationHeaderWithoutBearer() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 토큰이지만 공백인 경우")
    void bearerTokenWithEmptyValue() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken("");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰")
    void invalidJwtToken() throws ServletException, IOException {
        // given
        String invalidToken = "invalid-jwt-token";
        String bearerToken = "Bearer " + invalidToken;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(jwtTokenProvider, never()).getEmail(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰은 유효하지만 사용자를 찾을 수 없는 경우")
    void validTokenButUserNotFound() throws ServletException, IOException {
        // given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;
        String userEmail = "notfound@example.com";

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmail(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail))
                .thenThrow(new RuntimeException("User not found"));

        // when & then
        assertThatThrownBy(() ->
                jwtAuthenticationFilter.doFilter(request, response, filterChain)
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmail(validToken);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("JWT 토큰에서 이메일 추출 실패")
    void failToExtractEmailFromToken() throws ServletException, IOException {
        // given
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmail(validToken)).thenThrow(new RuntimeException("Failed to extract email"));

        // when & then
        assertThatThrownBy(() ->
                jwtAuthenticationFilter.doFilter(request, response, filterChain)
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to extract email");

        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmail(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("이미 인증된 상태에서 다시 인증 시도")
    void alreadyAuthenticated() throws ServletException, IOException {
        // given
        // 이미 인증된 상태 설정
        Authentication existingAuth = new UsernamePasswordAuthenticationToken(
                testUserDetails, null, testUserDetails.getAuthorities()
        );
        when(securityContext.getAuthentication()).thenReturn(existingAuth);

        // 불필요한 스터빙 제거
        // when(request.getHeader("Authorization")).thenReturn(bearerToken);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        // 이미 인증된 상태에서는 토큰 검증 메서드를 호출하지 않음
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getEmail(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("SecurityContext가 null인 경우")
    void securityContextIsNull() throws ServletException, IOException {
        // given
        SecurityContextHolder.clearContext();
        String validToken = "valid-jwt-token";
        String bearerToken = "Bearer " + validToken;
        String userEmail = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmail(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(testUserDetails);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmail(validToken);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(filterChain).doFilter(request, response);

        // SecurityContext가 새로 생성되어 인증이 설정되었는지 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("여러 번의 Bearer 문자열이 포함된 경우")
    void multipleBearerInHeader() throws ServletException, IOException {
        // given
        String tokenWithBearer = "Bearer valid-jwt-token Bearer extra";

        when(request.getHeader("Authorization")).thenReturn(tokenWithBearer);
        when(jwtTokenProvider.validateToken("valid-jwt-token Bearer extra")).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken("valid-jwt-token Bearer extra");
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
}