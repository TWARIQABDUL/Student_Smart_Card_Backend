package com.student_smart_pay.student_management.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}