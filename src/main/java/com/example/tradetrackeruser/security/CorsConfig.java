//package com.example.tradetrackeruser.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    @Primary
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//
//        corsConfig.addExposedHeader("Authorization");
//        corsConfig.addExposedHeader("RefreshToken");
//        corsConfig.addAllowedOriginPattern("http://localhost:3000");
//        corsConfig.addAllowedHeader("*");
//        corsConfig.addAllowedMethod("*");
//        corsConfig.setAllowCredentials(true);
//
//        // URL 패턴에 따라 CORS 설정을 설정하는 객체
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//
//        return source;
//    }
//}
