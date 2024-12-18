package com.example.tradetrackeruser.security;

import com.example.tradetrackeruser.service.TokenService;
import com.example.tradetrackeruser.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfiguration {

    private final UserService userService;
    private final TokenService tokenService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        JWTLoginFilter loginFilter = new JWTLoginFilter(authenticationManager, userService, tokenService);
        JWTCheckFilter checkFilter = new JWTCheckFilter(authenticationManager, userService);
//        RedisLoginFilter redisLoginFilter = new RedisLoginFilter(tokenService);


        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 관리 정책 설정 (세션 생성 x)
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/authenticate").permitAll()
                                .requestMatchers("/api/v1/auth/test001").permitAll() // 인증 없이 접근 가능
                                // 인증 없이 접근 가능
//                                .requestMatchers("/actuator/health").permitAll() // Actuator의 health 엔드포인트도 인증 없이 접근 가능
                                .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
                )
//
//                .authorizeHttpRequests(auth ->
//                        auth.anyRequest().permitAll() // 모든 요청에 대해 인증 없이 허용
//                )
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class) // 로그인 필터 추가
//                .addFilterAt(checkFilter, BasicAuthenticationFilter.class) // JWT 검증 필터 추가
                .addFilterBefore(checkFilter, AuthorizationFilter.class) // JWT 검증 필터 AuthorizationFilter.class 전에 등록
                .exceptionHandling(ex ->
                        ex
                                .authenticationEntryPoint(customAuthenticationEntryPoint) // 필터 이후 처리
        );
        return http.build(); // SecurityFilterChain 반환
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    // 원본 사용 (비밀번호 인고딩 x)
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // 비밀번호 인코딩 방식을 NoOp으로 설정
    }
}
