package com.example.tradetrackeruser.controller;


import com.example.tradetrackeruser.dto.Passport;
import com.example.tradetrackeruser.dto.UserRegisterDto;
import com.example.tradetrackeruser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        userService.logoutUser();
        return ResponseEntity.status(HttpStatus.OK).body("로그아웃 성공");
    }

    @GetMapping("/authenticate")
    public ResponseEntity<Passport> getUserAuthenticate() {
        Passport passport = userService.getUserInfo();
        if (passport != null) {
            return ResponseEntity.ok(passport);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }




}
