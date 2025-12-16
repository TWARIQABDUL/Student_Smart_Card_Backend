package com.student_smart_pay.student_management.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private String name;
    private String email;
    private String nfcToken;      // "STU-2025-..."
    private Roles role;           // STUDENT, GUARD, etc.
    private BigDecimal walletBalance;
    private LocalDateTime validUntil;
    private boolean isActive;

    // Constructor to map Entity -> DTO
    public UserDto(String name, String email, String nfcToken, Roles role, BigDecimal walletBalance, LocalDateTime validUntil, boolean isActive) {
        this.name = name;
        this.email = email;
        this.nfcToken = nfcToken;
        this.role = role;
        this.walletBalance = walletBalance;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }
}