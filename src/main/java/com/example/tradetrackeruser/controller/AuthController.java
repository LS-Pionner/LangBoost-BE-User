package com.example.tradetrackeruser.controller;


import com.example.api.response.ApiResponse;
import com.example.tradetrackeruser.dto.*;
import com.example.tradetrackeruser.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @Autowired
    private UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto);
        return ApiResponse.ok("회원가입 성공");
    }

    @GetMapping("/email-check")
    public ApiResponse<String> checkEmailAvailable(@RequestParam(name = "email") String email) {
        userService.checkEmailExists(email);
        return ApiResponse.ok("사용가능한 이메일입니다.");
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ApiResponse<String> logout(HttpServletResponse response) {
        userService.logoutUser();

        response.addHeader("Set-Cookie", "RefreshToken=; Max-Age=0; path=/; SameSite=Lax"); // 브라우저에 저장된 쿠키 삭제
        return ApiResponse.ok("로그아웃 성공");
    }

    @PostMapping("/authenticate")
    public ApiResponse<Passport> getUserAuthenticate(@RequestBody VerifyResult verifyResult) {
        Passport passport = userService.getUserInfo(verifyResult);
        return ApiResponse.ok(passport);
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<UserInfoDto> login(@RequestBody UserLoginForm loginForm, HttpServletResponse response) {
        UserInfoAndTokenDto userInfoAndTokenDto = userService.loginUser(loginForm);

        // 쿠키로 전달
        ResponseCookie cookie = ResponseCookie
                .from("RefreshToken", userInfoAndTokenDto.tokenDto().refreshToken())
                .maxAge(60 * 60 * 24 * 7)   // 7일
                .path("/")
                // Https 환경에서만 동작
//                .secure(true)
                .sameSite("Lax")
                .httpOnly(true)
                .build();

        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userInfoAndTokenDto.tokenDto().accessToken());
        response.setHeader("Set-Cookie", cookie.toString());
//        response.setHeader("refresh_token", userInfoAndTokenDto.tokenDto().refreshToken());

        return ApiResponse.ok(userInfoAndTokenDto.userInfoDto());
    }

    // 토큰 재발급
    @PostMapping("/auth/reissue")
    public ApiResponse<String> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("refresh_token");

        TokenDto tokenDto = userService.reissueToken(refreshToken);

        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.accessToken());
        response.setHeader("refresh_token", tokenDto.refreshToken());

        return ApiResponse.ok("재발급 성공");
    }
}
