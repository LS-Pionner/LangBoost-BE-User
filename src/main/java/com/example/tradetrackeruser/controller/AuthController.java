package com.example.tradetrackeruser.controller;


import com.example.api.response.ApiResponse;
import com.example.tradetrackeruser.dto.Passport;
import com.example.tradetrackeruser.dto.UserRegisterDto;
import com.example.tradetrackeruser.dto.VerifyResult;
import com.example.tradetrackeruser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto);
        return ApiResponse.ok("회원가입 성공");
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        userService.logoutUser();
        return ApiResponse.ok("로그아웃 성공");
    }

    @PostMapping("/authenticate")
    public ApiResponse<Passport> getUserAuthenticate(@RequestBody VerifyResult verifyResult) {
        Passport passport = userService.getUserInfo(verifyResult);
        return  ApiResponse.ok(passport);
    }





}
