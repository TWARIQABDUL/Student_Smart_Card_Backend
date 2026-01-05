package com.smartaccess.backend.models;

import com.smartaccess.backend.dto.Roles;
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
@Table(name = "users") // 👈 Refactored: Table is now "users"
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔒 Encrypted NFC Token
    @Column(unique = true, nullable = false)
    @NotBlank(message = "NFC Token is required")
    private String nfcToken;

    // 🔑 Dynamic QR Secret
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
    private Roles role = Roles.MEMBER; // 👈 Refactored: Default is MEMBER

    @Column(nullable = false)
    private LocalDateTime membershipExpiry; // 👈 Refactored: Was "validUntil"

    @Column(nullable = false)
    @Min(value = 0, message = "Balance cannot be negative")
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean isFirstLogin = true;

    private boolean isActive = true;

    // 👈 Refactored: mappedBy "user" (matches the field name in AccessLog)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessLog> accessLogs;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id") // 👈 Refactored: DB Column
    private Organization organization;    // 👈 Refactored: Variable name

    // =================================================================
    // AUTHENTICATION LOGIC
    // =================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return isActive; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}