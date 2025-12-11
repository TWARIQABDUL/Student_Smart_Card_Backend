package com.student_smart_pay.student_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "students")
@Data // Lombok generates Getters, Setters, toString, etc.
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This links to your physical card
    // "STUDENT-ID-12345-SECURE"
    @Column(unique = true, nullable = false)
    @NotBlank(message = "NFC Token is required")
    private String nfcToken;

    @NotBlank(message = "Name is required")
    private String name;

    @Column(unique = true)
    private String email;

    // Using BigDecimal is better for money than Double (precision errors)
    @Column(nullable = false)
    @Min(value = 0, message = "Balance cannot be negative")
    private BigDecimal walletBalance = BigDecimal.ZERO;

    private boolean isActive = true;
}