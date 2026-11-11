package com.datlichkhambenh.backend.factory;

import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Creator (GoF Factory Method). Mở tài khoản luôn giống nhau: lưu login đang hoạt động với mật khẩu đã mã hóa
 * và đúng vai trò, rồi tạo hồ sơ. Chỉ bước cuối khác nhau theo vai trò, nên đó là factory method mà mỗi
 * Concrete Creator cài đặt.
 */
public abstract class AccountFactory {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    protected AccountFactory(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /** Vai trò factory này tạo, như lưu trên user ("PATIENT", "DOCTOR"). */
    public abstract String role();

    public final ClinicAccount create(AccountRequest request) {
        User user = users.save(User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.rawPassword()))
                .role(role())
                .isActive(true)
                .build());
        return createProfile(user, request);
    }

    /** Factory method: dựng và lưu hồ sơ riêng của vai trò cho login vừa lưu. */
    protected abstract ClinicAccount createProfile(User user, AccountRequest request);
}
