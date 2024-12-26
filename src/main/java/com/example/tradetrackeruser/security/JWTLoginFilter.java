package com.example.tradetrackeruser.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.tradetrackeruser.dto.UserLoginForm;
import com.example.tradetrackeruser.dto.VerifyResult;
import com.example.tradetrackeruser.service.TokenService;
import com.example.tradetrackeruser.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.example.tradetrackeruser.entity.User;

import java.io.IOException;

public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;
    private TokenService tokenService;

    public JWTLoginFilter(AuthenticationManager authenticationManager, UserService userService, TokenService tokenService) {
        super(authenticationManager);
        this.userService = userService;
        this.tokenService = tokenService;
        setFilterProcessesUrl("/api/v1/login");
    }

    // 처음 사용자 로그인 or Refresh Token 존재
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException
    {
        UserLoginForm userLogin = objectMapper.readValue(request.getInputStream(), UserLoginForm.class);

        // 처음 로그인 하는 사용자
        if(userLogin.getRefreshToken() == null) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userLogin.getUsername(), userLogin.getPassword(), null
            );
            // user details...
            return getAuthenticationManager().authenticate(token);

            // 과거에 로그인 한 경험이 있는 유저
        }else{
            VerifyResult verify = JWTUtil.verify(userLogin.getRefreshToken());

            // refresh token 유효기간 O
            if(verify.isSuccess()){
                User user = (User) userService.loadUserByUsername(verify.username());

//                // Refresh Token Redis 확인
                if (tokenService.isRefreshTokenValid(user.getEmail(), userLogin.getRefreshToken())) {
                    return new UsernamePasswordAuthenticationToken(user, user.getAuthorities());
                } else {
                    throw new AuthenticationException("Refresh token is invalid or does not match.") {};
                }
//                return new UsernamePasswordAuthenticationToken(user, user.getAuthorities());
                // refresh token 유효기간 x
            }else{
                throw new TokenExpiredException("refresh token expired");
            }
        }
    }


    // 인증 성공 후 토큰 및 body response
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException
    {
        User user = (User) authResult.getPrincipal();

        // 필터를 따로 쓸건지 고민
        String refreshToken = JWTUtil.makeRefreshToken(user);

        tokenService.saveRefreshTokenToRedis(user.getEmail(),refreshToken, JWTUtil.REFRESH_TIME );

        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + JWTUtil.makeAccessToken(user));
        response.setHeader("refresh_token", refreshToken);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(user));
    }
}
