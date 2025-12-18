package com.student_smart_pay.student_management.models;

import com.student_smart_pay.student_management.dto.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // <--- CRITICAL IMPORT

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "students")
@Data
public class Student implements UserDetails { // <--- 1. ADD THIS INTERFACE

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

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role = Roles.STUDENT;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    @Min(value = 0, message = "Balance cannot be negative")
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean isFirstLogin = true;

    private boolean isActive = true;

    // Optional Relationship (if you added it)
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessLog> accessLogs;
    
    @ManyToOne(fetch = FetchType.EAGER) // Load campus details automatically on login
    @JoinColumn(name = "campus_id")
    private Campus campus;

    // =================================================================
    // 👇 2. IMPLEMENT REQUIRED METHODS (Authentication Logic)
    // =================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Tells Spring what "Role" this user has
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email; // We use EMAIL as the username
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive; // If isActive=false, account is locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive; // Only allow login if active
    }
}