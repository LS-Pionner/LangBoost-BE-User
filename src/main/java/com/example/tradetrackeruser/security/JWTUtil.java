package com.example.tradetrackeruser.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.tradetrackeruser.dto.VerifyResult;

import com.example.tradetrackeruser.entity.User;

import java.time.Instant;

public class JWTUtil {

    private static final Algorithm ALGORITHM = Algorithm.HMAC256("jimmy");
    private static final long AUTH_TIME = 60 * 60; // 60분
    public static final long REFRESH_TIME = 60 * 60 * 24 * 7; // 7일

    // @Value로 application.yml에서 secret 값을 주입
//    public JWTUtil(@Value("${jwt.secret}") String secret) {
//        this.ALGORITHM = Algorithm.HMAC256(secret);
//    }

    // jwt 토큰 생성 (payload : 유저 이메일)
    public static String makeAccessToken(User user){
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("iat", Instant.now().getEpochSecond()) // 발급 시간
                .withClaim("exp", Instant.now().getEpochSecond() + AUTH_TIME) // 만료 시간
                .sign(ALGORITHM);
    }

    public static String makeRefreshToken(User user){
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("iat", Instant.now().getEpochSecond()) // 발급 시간
                .withClaim("exp", Instant.now().getEpochSecond() + REFRESH_TIME)
                .sign(ALGORITHM);
    }

    // jwt 검증
    public static VerifyResult verify(String token){
        try {
            // 검증 성공
            DecodedJWT verify = JWT.require(ALGORITHM).build().verify(token);
            return new VerifyResult(true, verify.getSubject());

        }catch(Exception ex){
            // 검증 실패
            // ?! sub에 아무 내용이 없으면 어떻게 되지 ?!
            DecodedJWT decode = JWT.decode(token);
            return new VerifyResult(false, decode.getSubject());
        }
    }
}
