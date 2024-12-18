package com.example.tradetrackeruser.security.exception;
import org.springframework.security.core.AuthenticationException;


public class JWTAuthenticationException extends AuthenticationException {

    public JWTAuthenticationException(String message) {
        super(message);
    }

    public JWTAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

