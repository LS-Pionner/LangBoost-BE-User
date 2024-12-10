package com.example.tradetrackeruser.controller;


import com.example.api.response.ApiResponse;
import com.example.tradetrackeruser.dto.*;
import com.example.tradetrackeruser.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @Autowired
    private UserService userService;

    // 회원가입
    @PostMapping("/auth/register")
    public ApiResponse<String> register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto);
        return ApiResponse.ok("회원가입 성공");
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ApiResponse<String> logout() {
        userService.logoutUser();
        return ApiResponse.ok("로그아웃 성공");
    }

    @PostMapping("/auth/authenticate")
    public ApiResponse<Passport> getUserAuthenticate(@RequestBody VerifyResult verifyResult) {
        Passport passport = userService.getUserInfo(verifyResult);
        return ApiResponse.ok(passport);
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<UserInfoDto> login(@RequestBody UserLoginForm loginForm, HttpServletResponse response) {
        UserInfoAndTokenDto userInfoAndTokenDto = userService.loginUser(loginForm);

        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userInfoAndTokenDto.tokenDto().accessToken());
        response.setHeader("refresh_token", userInfoAndTokenDto.tokenDto().refreshToken());

        return ApiResponse.ok(userInfoAndTokenDto.userInfoDto());
    }



}
