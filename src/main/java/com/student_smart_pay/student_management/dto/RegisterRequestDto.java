package com.student_smart_pay.student_management.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    // --- USER INPUTS ---
    private String name;
    private String email;
    private String password;
    
    // Default to STUDENT if not sent
    private Roles role = Roles.STUDENT;

    // Optional: Only used if Super Admin is registering someone
    private Long campusId; 
}