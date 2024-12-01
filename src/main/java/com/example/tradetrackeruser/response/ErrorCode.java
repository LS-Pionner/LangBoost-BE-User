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


    // 404 Not Found
    NOT_FOUND_USER(40401, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
