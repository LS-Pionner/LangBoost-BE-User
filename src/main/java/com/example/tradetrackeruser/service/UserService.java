package com.example.tradetrackeruser.service;

import com.example.api.response.CustomException;
import com.example.tradetrackeruser.dto.*;
import com.example.tradetrackeruser.entity.RoleType;
import com.example.tradetrackeruser.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tradetrackeruser.repository.UserRepository;
import com.example.tradetrackeruser.entity.User;
import com.example.tradetrackeruser.response.ErrorCode;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(
                ()->new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public User createUser(UserRegisterDto userRegisterDto) {
        checkEmailExists(userRegisterDto.email());

        User user = User.builder()
                .email(userRegisterDto.email())
                .password(passwordEncoder.encode(userRegisterDto.password()))
                .roleType(RoleType.NOBODY)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    public void checkEmailExists(String email) {
        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    public boolean logoutUser() {
        // SecurityContext에서 현재 인증된 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName(); // 사용자의 이름 또는 ID

            // Refresh Token 삭제
            tokenService.deleteRefreshToken(username);

            // SecurityContext 초기화
            SecurityContextHolder.clearContext();
            return true;
        }

        // 인증 정보가 없는 경우 (로그아웃 실패)
        throw new CustomException(ErrorCode.NOT_FOUND_USER); // 사용자 정보 없음
    }

    public Passport getUserInfo(VerifyResult verifyResult) {
        String username = verifyResult.username();
        Optional<User> user = userRepository.findUserByEmail(username);

        log.info("User found: {}", user);

        return user.map(u -> new Passport(u.getId(), u.getUsername()))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public UserInfoAndTokenDto loginUser(UserLoginForm loginForm) {
        loadUserByUsername(loginForm.getUsername());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginForm.getUsername(), loginForm.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        String accessToken = JWTUtil.makeAccessToken(user);
        String refreshToken = JWTUtil.makeRefreshToken(user);
        tokenService.saveRefreshTokenToRedis(user.getEmail(), refreshToken, JWTUtil.REFRESH_TIME);

        TokenDto tokenDto = new TokenDto(accessToken, refreshToken);
        UserInfoDto userInfoDto = new UserInfoDto(user.getId(), user.getEmail(), user.getUsername(), user.getPassword(), user.isEnabled());

        return new UserInfoAndTokenDto(userInfoDto, tokenDto);
    }

    public TokenDto reissueToken(String refreshToken) {
        VerifyResult verifyResult = JWTUtil.verify(refreshToken);

        // 전달받은 refresh 토큰이 유효하지 않음
        if (!verifyResult.isSuccess()) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = (User) loadUserByUsername(verifyResult.username());

        // 전달받은 refresh 토큰이 저장된 토큰과 일치하지 않음
        if (!tokenService.isRefreshTokenValid(user.getUsername(), refreshToken)) {
            throw new CustomException(ErrorCode.NOT_MATCHED_REFRESH_TOKEN);
        }

        String reissuedAccessToken = JWTUtil.makeAccessToken(user);
        String reissuedRefreshToken = JWTUtil.makeRefreshToken(user);
        tokenService.saveRefreshTokenToRedis(user.getEmail(), reissuedRefreshToken, JWTUtil.REFRESH_TIME);

        return new TokenDto(reissuedAccessToken, reissuedRefreshToken);
    }
}
