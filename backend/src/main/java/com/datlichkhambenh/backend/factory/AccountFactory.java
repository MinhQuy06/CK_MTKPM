package com.datlichkhambenh.backend.factory;

import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Creator (GoF Factory Method). Opening an account is always the same: save an active login with a
 * hashed password and the right role, then create the profile. Only that last step differs per role,
 * so it is the factory method each concrete creator implements.
 */
public abstract class AccountFactory {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    protected AccountFactory(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /** Role this factory creates, as stored on the user ("PATIENT", "DOCTOR"). */
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

    /** Factory method: build and save the role-specific profile for a freshly saved login. */
    protected abstract ClinicAccount createProfile(User user, AccountRequest request);
}
