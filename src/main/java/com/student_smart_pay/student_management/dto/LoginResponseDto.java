package com.student_smart_pay.student_management.dto;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private String message;
    private UserDto user;

    public LoginResponseDto(String token, String message, UserDto user) {
        this.token = token;
        this.message = message;
        this.user = user;
    }
}