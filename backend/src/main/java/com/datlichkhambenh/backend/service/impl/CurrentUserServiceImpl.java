package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.repository.UserRepository;
import com.datlichkhambenh.backend.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));
    }
}
