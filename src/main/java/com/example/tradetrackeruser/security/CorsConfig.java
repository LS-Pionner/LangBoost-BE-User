package com.example.tradetrackeruser.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

//    @Value("${frontend.url}")
//    private String frontendUrl;
//
//    @Value("${backend.gateway.url}")
//    private String gatewayUrl;


    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 허용된 Origin 추가
        corsConfig.addAllowedOriginPattern("http://localhost:3000");
        corsConfig.addAllowedOriginPattern("http://localhost:8080");

        // 요청 해더 허용
        corsConfig.addAllowedMethod("*");

        // 응답 해더 노출
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("RefreshToken");
        corsConfig.addExposedHeader("x-passport");

        // 자격 증명 허용
        corsConfig.setAllowCredentials(true);

        // URL 패턴에 따라 CORS 설정을 설정하는 객체
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }
}
