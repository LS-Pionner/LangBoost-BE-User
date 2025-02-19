package com.example.tradetrackeruser.security;

import com.example.tradetrackeruser.entity.RoleType;
import com.example.tradetrackeruser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserService userService;

    private final JWTUtil jwtUtil;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        JWTCheckFilter checkFilter = new JWTCheckFilter(authenticationManager, userService, jwtUtil);

        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 관리 정책 설정 (세션 생성 x)
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/authenticate", "/api/v1/auth/login", "/api/v1/auth/email-check").permitAll() // 이 경로들은 인증 없이 접근 가능
                                .requestMatchers("/api/v1/auth/logout", "/api/v1/auth/reissue").authenticated() // 로그아웃 & 토큰 재발급은 권한에 상관없이 접근 가능
                                .requestMatchers("/api/v1/auth/**").hasAnyRole(RoleType.USER.name(), RoleType.ADMIN.name()) // 나머지 모든 요청은 USER 권한 필요
                                .anyRequest().permitAll()
                ).exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterAt(checkFilter, BasicAuthenticationFilter.class); // JWT 검증 필터 추가
        return http.build(); // SecurityFilterChain 반환
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 비밀번호 해시 방식 적용
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 원본 사용 (비밀번호 인고딩 x)
//    @Bean
//    public static PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance(); // 비밀번호 인코딩 방식을 NoOp으로 설정
//    }
}
