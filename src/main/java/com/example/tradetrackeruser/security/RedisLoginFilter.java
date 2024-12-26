package com.example.tradetrackeruser.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.tradetrackeruser.service.TokenService;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class RedisLoginFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 필터 체인 진행
        filterChain.doFilter(request, response);

        // /login 경로에서만 필터가 작동하도록 설정
        if ("/api/v1/login".equals(request.getRequestURI())) {
            // 응답 처리 후 추가적인 작업
            logger.info("Post-login redis filter executed for /api/v1/login");

            String refreshToken = response.getHeader("refresh_token");

            if (refreshToken != null) {
                // refresh token을 Redis에서 검증하는 로직
//                boolean isValid = tokenService.saveRefreshTokenToRedis(refreshToken);

                }
            }
        }
    }

