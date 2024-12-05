package com.example.tradetrackeruser.response;

import com.example.api.response.ErrorCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements ErrorCodeInterface {

    // 400 BAD REQUEST
    INVALID_VERIFY_CODE(40001, HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),

    // 401 Unauthorized - 잘못된 토큰
    INVALID_TOKEN(40101, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 404 Not Found
    NOT_FOUND_USER(40401, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 403 Forbidden
    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),


    // 500 Internal Server Error
    TEST(50001, HttpStatus.INTERNAL_SERVER_ERROR, "테스트 에러");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}

