package com.student_smart_pay.student_management.models;

import com.student_smart_pay.student_management.dto.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "students")
@Data
public class Student implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔒 SECURITY UPDATE 1: This now stores the ENCRYPTED string (Ciphertext).
    // Reading the physical card will only show gibberish (e.g. "U2FsdGVkX1...").
    @Column(unique = true, nullable = false)
    @NotBlank(message = "NFC Token is required")
    private String nfcToken;

    // 🔑 SECURITY UPDATE 2: New field for Dynamic QR Codes.
    // This stores the unique "Seed" that the mobile app uses to generate timed codes.
    @Column(nullable = true) 
    private String qrSecret;

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

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessLog> accessLogs;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    // =================================================================
    // AUTHENTICATION LOGIC (Unchanged)
    // =================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
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
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}