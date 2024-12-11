package com.example.tradetrackeruser.service;

import com.example.api.response.CustomException;
import com.example.tradetrackeruser.dto.*;
import com.example.tradetrackeruser.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;  // PasswordEncoder 주입

    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(
                ()->new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public Optional<User> findUser(String email) {
        return userRepository.findUserByEmail(email);
    }

    public User createUser(UserRegisterDto userRegisterDto) {
        User user = new User();
        user.setEmail(userRegisterDto.email());
        user.setPassword(passwordEncoder.encode(userRegisterDto.password()));
        user.setEnabled(true);  // enabled true로 변경 후 로그인 시 계정 잠금 문제 해결 (기존 false)

        return userRepository.save(user);
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

        System.out.println("User found: " + user);

        return user.map(u -> new Passport(u.getId(), u.getUsername()))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public UserInfoAndTokenDto loginUser(UserLoginForm loginForm) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginForm.getUsername(), loginForm.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        User user = (User) authentication.getPrincipal();

        String accessToken = JWTUtil.makeAccessToken(user);
        String refreshToken = JWTUtil.makeRefreshToken(user);
        tokenService.saveRefreshTokenToRedis(user.getEmail(), refreshToken, JWTUtil.REFRESH_TIME);

        TokenDto tokenDto = new TokenDto(accessToken, refreshToken);
        UserInfoDto userInfoDto = new UserInfoDto(user.getId(), user.getEmail(), user.getUsername(), user.getPassword(), user.isEnabled());

        return new UserInfoAndTokenDto(userInfoDto, tokenDto);
    }

    public UserInfoAndTokenDto reissueToken(String refreshToken) {
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

        TokenDto tokenDto = new TokenDto(reissuedAccessToken, reissuedRefreshToken);
        UserInfoDto userInfoDto = new UserInfoDto(user.getId(), user.getEmail(), user.getUsername(), user.getPassword(), user.isEnabled());

        return new UserInfoAndTokenDto(userInfoDto, tokenDto);
    }


    // 권한 관련
/*    public void addAuthority(Long userId, String authority){
        userRepository.findById(userId).ifPresent(user->{
            SpAuthority newRole = new SpAuthority(user.getUserId(), authority);
            if(user.getAuthorities() == null){
                HashSet<SpAuthority> authorities = new HashSet<>();
                authorities.add(newRole);
                user.setAuthorities(authorities);
                save(user);
            }else if(!user.getAuthorities().contains(newRole)){
                HashSet<SpAuthority> authorities = new HashSet<>();
                authorities.addAll(user.getAuthorities());
                authorities.add(newRole);
                user.setAuthorities(authorities);
                save(user);
            }
        });
    }*/

    //권한 관련
/*    public void removeAuthority(Long userId, String authority){
        userRepository.findById(userId).ifPresent(user->{
            if(user.getAuthorities()==null) return;
            SpAuthority targetRole = new SpAuthority(user.getUserId(), authority);
            if(user.getAuthorities().contains(targetRole)){
                user.setAuthorities(
                        user.getAuthorities().stream().filter(auth->!auth.equals(targetRole))
                                .collect(Collectors.toSet())
                );
                save(user);
            }
        });
    }*/
}
