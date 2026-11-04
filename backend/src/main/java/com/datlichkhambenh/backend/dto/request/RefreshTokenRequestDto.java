package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;

    public RefreshTokenRequestDto() {}

    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public static RefreshTokenRequestDtoBuilder builder() {
        return new RefreshTokenRequestDtoBuilder();
    }

    public static class RefreshTokenRequestDtoBuilder {
        private String refreshToken;

        public RefreshTokenRequestDtoBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }

        public RefreshTokenRequestDto build() {
            return new RefreshTokenRequestDto(refreshToken);
        }
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
