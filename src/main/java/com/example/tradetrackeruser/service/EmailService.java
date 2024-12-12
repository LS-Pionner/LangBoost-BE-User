package com.example.tradetrackeruser.service;

import com.example.api.response.CustomException;
import com.example.api.response.DefaultErrorCode;
import com.example.tradetrackeruser.dto.EmailDto;
import com.example.tradetrackeruser.entity.RoleType;
import com.example.tradetrackeruser.repository.UserRepository;
import com.example.tradetrackeruser.response.ErrorCode;
import com.example.tradetrackeruser.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import com.example.tradetrackeruser.entity.User;

import java.util.Optional;
import java.util.Random;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;

    @Autowired
    private UserRepository userRepository;
    private static final String senderEmail = "jj@naver.com";

    // 인증 코드 생성
    // 무작위 6자리 숫자와 알파벳으로 이루어진 인증 코드
//    private String createCode() {
//        int leftLimit = 48; // number '0'
//        int rightLimit = 122; // alphabet 'z'
//        int targetStringLength = 6;
//        Random random = new Random();
//
//        return random.ints(leftLimit, rightLimit + 1)
//                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 | i >= 97))
//                .limit(targetStringLength)
//                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//                .toString();
//    }

    // 무작위 6자리 숫자
    private static String createCode() {
        int leftLimit = 48; // number '0'
        int rightLimit = 57; // number '9'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)  // 숫자 0부터 9까지
                .limit(targetStringLength)  // 6자리 길이로 제한
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


    // 이메일 내용 초기화
    private String setContext(String code) {
        Context context = new Context();
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        context.setVariable("code", code);

        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);

        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine.process("mail", context);
    }

    // 이메일 폼 생성
    private MimeMessage createEmailForm(EmailDto emailDto) throws MessagingException {
        String authCode = createCode();

        String purpose = emailDto.purpose();
        String email = emailDto.mail();

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("안녕하세요. 인증번호입니다.");
        message.setFrom(senderEmail);
        message.setText(setContext(authCode), "utf-8", "html");

        String redisKey = purpose + ":" + email;

        // Redis 에 해당 인증코드 인증 시간 설정
        redisUtil.setDataExpire(redisKey, authCode, 60 * 30L);

        return message;
    }

    // 인증코드 이메일 발송
    public void sendEmail(EmailDto emailDto) throws MessagingException {
        String toEmail = emailDto.mail();

        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }
        // 이메일 폼 생성
        MimeMessage emailForm = createEmailForm(emailDto);
        // 이메일 발송
        javaMailSender.send(emailForm);
    }

    // 코드 검증
    public Boolean verifyEmailCode(EmailDto emailDto) {

        String purpose = emailDto.purpose();
        String email = emailDto.mail();
        String code = emailDto.verifyCode();

        String redisKey = purpose + ":" + email;

        // Redis에서 코드 가져오기
        String codeFoundByEmail = redisUtil.getData(redisKey);
        log.info("code found by email: " + codeFoundByEmail);

        // 코드가 없거나 다르면 실패 처리
        if (codeFoundByEmail == null || !codeFoundByEmail.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFY_CODE); // 유효하지 않은 코드
        }

        // 이메일로 사용자 찾기
        Optional<User> optionalUser = userRepository.findUserByEmail(email);

        if (!optionalUser.isPresent()) {
            throw new CustomException(ErrorCode.NOT_FOUND_USER); // 사용자 정보 없음
        }

        // 사용자 존재하면 enabled 설정 후 저장
        User user = optionalUser.get();
        user.setRoleType(RoleType.USER);
//        user.setEnabled(true);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            // 사용자 저장 중 오류가 발생한 경우
            log.error("Failed to enable user with email: {}", email, e);
            throw new CustomException(DefaultErrorCode.INTERNAL_SERVER_ERROR); // 내부 서버 오류
        }

        // 성공적으로 활성화 처리된 경우
        return true;
    }


}
