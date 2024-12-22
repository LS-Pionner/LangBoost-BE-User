package com.example.tradetrackeruser.service;

import com.example.tradetrackeruser.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final RedisUtil redisUtil;

    // refresh token을 Redis에 저장
    public void saveRefreshTokenToRedis(String email, String refreshToken, long duration) {
        String key = "refresh_token:" + email;

        // refresh token을 Redis에 저장, 만료 시간도 설정
        redisUtil.setDataExpire(key, refreshToken, duration);
    }

    // refresh token이 유효한지 확인
    public boolean isRefreshTokenValid(String email, String refreshToken) {
        String key = "refresh_token:" + email;

        // Redis에서 해당 키의 값을 가져옴
        String storedRefreshToken = redisUtil.getData(key);

        // Redis에 저장된 refresh token 값이 null이 아니고, 입력된 refresh token과 동일한지 확인
        return storedRefreshToken != null && storedRefreshToken.equals(refreshToken);
    }

    // refresh token을 Redis에서 삭제
    public void deleteRefreshToken(String username) {
        String key = "refresh_token:" + username;

        // Redis에서 refresh token 삭제
        redisUtil.deleteData(key);
    }
}
