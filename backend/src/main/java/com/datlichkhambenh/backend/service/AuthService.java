package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.request.ChangePasswordDto;
import com.datlichkhambenh.backend.dto.request.LoginRequestDto;
import com.datlichkhambenh.backend.dto.request.RefreshTokenRequestDto;
import com.datlichkhambenh.backend.dto.request.RegisterRequestDto;
import com.datlichkhambenh.backend.dto.response.LoginResponseDto;
import com.datlichkhambenh.backend.dto.response.UserDto;

public interface AuthService {
    UserDto register(RegisterRequestDto request);
    LoginResponseDto login(LoginRequestDto request);
    LoginResponseDto refreshToken(RefreshTokenRequestDto request);
    void changePassword(String email, ChangePasswordDto request);
}
