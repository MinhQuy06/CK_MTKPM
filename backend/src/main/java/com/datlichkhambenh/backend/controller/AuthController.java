package com.datlichkhambenh.backend.controller;

import com.datlichkhambenh.backend.dto.request.ChangePasswordDto;
import com.datlichkhambenh.backend.dto.request.LoginRequestDto;
import com.datlichkhambenh.backend.dto.request.RefreshTokenRequestDto;
import com.datlichkhambenh.backend.dto.request.RegisterRequestDto;
import com.datlichkhambenh.backend.dto.response.ApiResponseDto;
import com.datlichkhambenh.backend.dto.response.LoginResponseDto;
import com.datlichkhambenh.backend.dto.response.UserDto;
import com.datlichkhambenh.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<UserDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        UserDto user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(user, "Đăng ký tài khoản thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Đăng nhập thành công"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        LoginResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Làm mới token thành công"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordDto request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "Đổi mật khẩu thành công"));
    }
}
