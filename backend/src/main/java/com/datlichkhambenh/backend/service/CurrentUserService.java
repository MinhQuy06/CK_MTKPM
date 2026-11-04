package com.datlichkhambenh.backend.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface CurrentUserService {
    Long getUserId(UserDetails userDetails);
}
