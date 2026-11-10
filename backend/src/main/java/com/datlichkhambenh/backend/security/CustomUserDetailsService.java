package com.datlichkhambenh.backend.security;

import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.UserRepository;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CustomUserDetailsService: nạp UserDetails từ DB theo email.
 * Spring Security dùng khi xác thực.
 *
 * Authority đặt thẳng theo role (ADMIN / DOCTOR / PATIENT),
 * không thêm tiền tố ROLE_ để khớp với hasAuthority() trên Controller.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public CustomUserDetailsService(UserRepository userRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Khong tim thay tai khoan voi email: " + email));

        // Boolean dạng wrapper: Lombok @Getter tạo getIsActive() (không phải isIsActive())
        boolean isActive = Boolean.TRUE.equals(user.getIsActive());
        boolean isApprovedDoctor = !"DOCTOR".equals(user.getRole())
                || doctorRepository.findById(user.getId())
                .map(doctor -> Boolean.TRUE.equals(doctor.getIsApproved()))
                .orElse(false);
        boolean canAccess = isActive && isApprovedDoctor;

        // Dùng thẳng user.getRole() (ví dụ: "ADMIN"), không thêm "ROLE_"
        // để khớp với @PreAuthorize("hasAuthority('ADMIN')") trên Controller
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .accountLocked(!canAccess)
                .disabled(!isActive)
                .build();
    }
}
