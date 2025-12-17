package com.student_smart_pay.student_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.student_smart_pay.student_management.dto.Roles;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Import Time

@Entity
@Table(name = "students")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "NFC Token is required")
    private String nfcToken;

    @NotBlank(message = "Name is required")
    private String name;

    @Column(unique = true)
    private String email;

    // --- SECURITY ---
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role = Roles.STUDENT;

    // --- NEW VALIDITY FIELD ---
    // Stores when this card expires
    @Column(nullable = false)
    private LocalDateTime validUntil; 

    // --- WALLET ---
    @Column(nullable = false)
    @Min(value = 0, message = "Balance cannot be negative")
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean isFirstLogin = true;
    private boolean isActive = true;
}