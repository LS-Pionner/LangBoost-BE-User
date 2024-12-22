package com.example.tradetrackeruser.security;

import com.example.tradetrackeruser.dto.VerifyResult;
import com.example.tradetrackeruser.security.exception.JWTAuthenticationException;
import com.example.tradetrackeruser.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;





import com.example.tradetrackeruser.entity.User;

import java.io.IOException;

@Slf4j
public class JWTCheckFilter extends BasicAuthenticationFilter {

    private UserService userService;

    public JWTCheckFilter(AuthenticationManager authenticationManager, UserService userService) {
        super(authenticationManager);
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 아래 경로에 대해서는 필터 X
        if (request.getRequestURI().equals("/api/v1/register") || request.getRequestURI().equals("/api/v1/authenticate")
                || request.getRequestURI().equals("/api/v1/login") || request.getRequestURI().equals("/api/v1/email-check")) {
            chain.doFilter(request, response);
            return;
        }

        // 토큰 존재 x
        if(bearer == null || !bearer.startsWith("Bearer ")){
            log.error("Invalid Authorization header format. Header: {}", bearer);
            // 예외를 던져 EntryPoint에서 ;해결
            throw new JWTAuthenticationException("Authorization header is missing");
        }

        // 토큰 존재 o
        String token = bearer.substring("Bearer ".length());

        // 토큰 검증
        VerifyResult result = JWTUtil.verify(token);

        if(result.isSuccess()){
            // 유저 검증
            User user = (User) userService.loadUserByUsername(result.username());
            UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(userToken);
            log.info("Token successfully verified for user: {}. Request URI: {}", user.getUsername(), request.getRequestURI());
            chain.doFilter(request, response);
        }else{
            // 검증 실패 시 로그 기록
            log.error("Token verification failed for token: {}.", token);

            throw new JWTAuthenticationException("Invalid or expired token.");
        }
    }

}

