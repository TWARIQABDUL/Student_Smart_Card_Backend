package com.student_smart_pay.student_management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {
    private String name;
    private String email;
    private String password;
    private Roles role = Roles.STUDENT;

    // Getters and Setters
}
