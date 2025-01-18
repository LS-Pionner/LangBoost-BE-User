package com.example.tradetrackeruser.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.tradetrackeruser.dto.VerifyResult;
import com.example.tradetrackeruser.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JWTUtil {

    private final Algorithm algorithm;
    private final long authTime;
    private final long refreshTime;

    public JWTUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.auth-time}") long authTime,
            @Value("${jwt.refresh-time}") long refreshTime
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.authTime = authTime;
        this.refreshTime = refreshTime;
    }


    // refreshTime에 대한 getter 메서드 추가
    public long getRefreshTime() {
        return refreshTime;
    }

    // JWT Access Token 생성
    public String makeAccessToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("iat", Instant.now().getEpochSecond()) // 발급 시간
                .withClaim("exp", Instant.now().getEpochSecond() + authTime) // 만료 시간
                .sign(algorithm);
    }

    // JWT Refresh Token 생성
    public String makeRefreshToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("iat", Instant.now().getEpochSecond()) // 발급 시간
                .withClaim("exp", Instant.now().getEpochSecond() + refreshTime)
                .sign(algorithm);
    }

    // JWT 검증
    public VerifyResult verify(String token) {
        try {
            // 검증 성공
            DecodedJWT verify = JWT.require(algorithm).build().verify(token);
            return new VerifyResult(true, verify.getSubject());
        } catch (Exception ex) {
            // 검증 실패
            DecodedJWT decode = JWT.decode(token);
            return new VerifyResult(false, decode.getSubject());
        }
    }
}
