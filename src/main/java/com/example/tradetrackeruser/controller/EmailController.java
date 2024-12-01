package com.example.tradetrackeruser.controller;

import com.example.tradetrackeruser.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tradetrackeruser.dto.EmailDto;

import com.example.api.response.ApiResponse;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
public class EmailController {
    private final EmailService emailService;

    // 인증코드 메일 발송
    @PostMapping("/send")
    public ApiResponse<String> mailSend(@RequestBody EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        emailService.sendEmail(emailDto);
        String message = "인증코드가 발송되었습니다.";
        return ApiResponse.ok(message);
    }


    // 인증코드 인증
    @PostMapping("/verify")
    public ApiResponse<String> verify(@RequestBody EmailDto emailDto) {
        log.info("EmailController.verify()");
        boolean isVerify = emailService.verifyEmailCode(emailDto);

        return ApiResponse.ok("인증이 완료되었습니다.");
    }
}
