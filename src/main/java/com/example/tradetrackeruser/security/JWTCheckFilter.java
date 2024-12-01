package com.example.tradetrackeruser.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.tradetrackeruser.dto.VerifyResult;
import com.example.tradetrackeruser.security.JWTUtil;
import com.example.tradetrackeruser.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.tradetrackeruser.entity.User;

import java.io.IOException;

public class JWTCheckFilter extends BasicAuthenticationFilter {

    private UserService userService;

    public JWTCheckFilter(AuthenticationManager authenticationManager, UserService userService) {
        super(authenticationManager);
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 토큰 존재 x
        if(bearer == null || !bearer.startsWith("Bearer ")){
            chain.doFilter(request, response);
            return;
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
            chain.doFilter(request, response);
        }else{
            throw new TokenExpiredException("Token is not valid");
        }
    }

}

