package da_ni_ni.backend.user.jwt;

import da_ni_ni.backend.user.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 이미 인증된 상태인지 확인
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.info("[JwtFilter] 이미 인증된 상태, 추가 인증 처리 건너뜀");
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        logger.info("[JwtFilter] Authorization Header: " + request.getHeader("Authorization"));
        logger.info("[JwtFilter] Extracted Token: " + token);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            logger.info("[JwtFilter] Token is valid");

            String email = jwtTokenProvider.getEmail(token);
            logger.info("[JwtFilter] Extracted Email: " + email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            logger.info("[JwtFilter] Loaded UserDetails: " + userDetails.getUsername());

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            logger.info("[JwtFilter] SecurityContext에 인증 객체 설정 완료");
        } else {
            logger.warn("[JwtFilter] 유효하지 않거나 누락된 토큰");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}


