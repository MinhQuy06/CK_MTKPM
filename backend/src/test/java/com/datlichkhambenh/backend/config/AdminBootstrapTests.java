package com.datlichkhambenh.backend.config;

import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapTests {
    private UserRepository users;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
    }

    private AdminBootstrap bootstrap(String email, String password) {
        return new AdminBootstrap(users, encoder, email, password);
    }

    @Test
    void createsFirstAdminWithHashedPasswordWhenNoAdminExists() {
        when(users.countByRoleAndIsActiveTrue("ADMIN")).thenReturn(0L);
        when(users.existsByEmailIgnoreCase("boss@clinic.vn")).thenReturn(false);

        bootstrap(" boss@clinic.vn ", "S3cretPass!").run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("boss@clinic.vn");
        assertThat(saved.getValue().getRole()).isEqualTo("ADMIN");
        assertThat(saved.getValue().getIsActive()).isTrue();
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("S3cretPass!");
        assertThat(encoder.matches("S3cretPass!", saved.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void doesNothingWhenAnAdminAlreadyExists() {
        when(users.countByRoleAndIsActiveTrue("ADMIN")).thenReturn(1L);

        bootstrap("boss@clinic.vn", "S3cretPass!").run(null);

        verify(users, never()).save(any());
    }

    @Test
    void rejectsShortPasswordOrBadEmail() {
        when(users.countByRoleAndIsActiveTrue("ADMIN")).thenReturn(0L);

        assertThatThrownBy(() -> bootstrap("boss@clinic.vn", "short").run(null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> bootstrap("not-an-email", "S3cretPass!").run(null))
                .isInstanceOf(IllegalStateException.class);
        verify(users, never()).save(any());
    }

    @Test
    void refusesToReuseEmailOfNonAdminAccount() {
        when(users.countByRoleAndIsActiveTrue("ADMIN")).thenReturn(0L);
        when(users.existsByEmailIgnoreCase("patient@x.vn")).thenReturn(true);

        assertThatThrownBy(() -> bootstrap("patient@x.vn", "S3cretPass!").run(null))
                .isInstanceOf(IllegalStateException.class);
        verify(users, never()).save(any());
    }
}
