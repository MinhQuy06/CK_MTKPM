package com.datlichkhambenh.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String role;
    private String fullName;
}
