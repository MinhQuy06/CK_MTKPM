package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.dto.request.LoginRequestDto;
import com.datlichkhambenh.backend.dto.request.RefreshTokenRequestDto;
import com.datlichkhambenh.backend.dto.request.RegisterRequestDto;
import com.datlichkhambenh.backend.dto.response.LoginResponseDto;
import com.datlichkhambenh.backend.dto.response.UserDto;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.factory.AccountRequest;
import com.datlichkhambenh.backend.factory.AccountFactories;
import com.datlichkhambenh.backend.factory.AccountFactory;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.UserRepository;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import com.datlichkhambenh.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountFactories accounts;

    @Override
    @Transactional
    public UserDto register(RegisterRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException("Email đã tồn tại trên hệ thống");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && (patientRepository.existsByPhone(request.getPhone()) || doctorRepository.existsByPhone(request.getPhone()))) {
            throw new AppException("Số điện thoại đã tồn tại trên hệ thống");
        }

        // Only roles with a registered factory can sign up: there is no ADMIN factory by design
        String role = request.getRole().toUpperCase();
        AccountFactory factory = accounts.forRole(role)
                .orElseThrow(() -> new AppException("Chỉ được đăng ký tài khoản PATIENT hoặc DOCTOR"));
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new AppException("Số điện thoại không được để trống");
        }
        if ("DOCTOR".equals(role)
                && (request.getExperienceYears() == null || request.getConsultationFee() == null)) {
            throw new AppException("Bác sĩ phải cung cấp số năm kinh nghiệm và giá khám");
        }
        User savedUser = factory.create(accountRequest(email, request)).user();

        return UserDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        ensureAccountCanAuthenticate(user);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        String fullName = resolveFullName(user);

        return LoginResponseDto.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(fullName)
                .build();
    }

    @Override
    public LoginResponseDto refreshToken(RefreshTokenRequestDto request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isRefreshToken(token)) {
            throw new AppException("Refresh Token không hợp lệ hoặc đã hết hạn");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        ensureAccountCanAuthenticate(user);

        String newToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        String fullName = resolveFullName(user);

        return LoginResponseDto.builder()
                .accessToken(newToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(fullName)
                .build();
    }

    private String resolveFullName(User user) {
        if ("DOCTOR".equals(user.getRole())) {
            return doctorRepository.findById(user.getId()).map(Doctor::getFullName).orElse("Bác sĩ");
        } else if ("PATIENT".equals(user.getRole())) {
            return patientRepository.findById(user.getId()).map(Patient::getFullName).orElse("Bệnh nhân");
        } else if ("ADMIN".equals(user.getRole())) {
            return "Quản trị viên";
        }
        return user.getEmail();
    }

    private void ensureAccountCanAuthenticate(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
        }
        if ("DOCTOR".equals(user.getRole())) {
            Doctor doctor = doctorRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ"));
            if (!Boolean.TRUE.equals(doctor.getIsApproved())) {
                throw new AppException("Tài khoản bác sĩ chưa được Admin phê duyệt", HttpStatus.FORBIDDEN);
            }
        }
    }

    /** Self-registered doctors wait for Admin approval. */
    private AccountRequest accountRequest(String email, RegisterRequestDto r) {
        return new AccountRequest(email, r.getPassword(), r.getFullName(), r.getPhone(), r.getDob(), r.getGender(),
                r.getAddress(), r.getMedicalHistory(), r.getSpecialtyId(), r.getDegree(), r.getExperienceYears(),
                r.getConsultationFee(), false);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional
    public void changePassword(String email, com.datlichkhambenh.backend.dto.request.ChangePasswordDto request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException("Mật khẩu hiện tại không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
