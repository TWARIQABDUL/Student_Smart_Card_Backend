package com.smartaccess.backend.dto;

import com.smartaccess.backend.models.User;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String nfcToken;      
    private Roles role;           
    private BigDecimal walletBalance;
    private LocalDateTime membershipExpiry; // 👈 Renamed from validUntil
    private boolean isActive;
    
    // 🔑 QR Secret for the Mobile App
    private String qrSecret; 
    
    // 🏢 Organization Details
    private OrganizationDto organization; // 👈 Renamed from campus

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.nfcToken = user.getNfcToken();
        this.role = user.getRole();
        this.walletBalance = user.getWalletBalance();
        this.membershipExpiry = user.getMembershipExpiry(); // 👈 New field
        this.isActive = user.isActive();
        
        this.qrSecret = user.getQrSecret(); 

        // Map Organization if it exists
        if (user.getOrganization() != null) {
            this.organization = new OrganizationDto(
                user.getOrganization().getName(),
                user.getOrganization().getLogoUrl(),
                user.getOrganization().getPrimaryColor(),
                user.getOrganization().getSecondaryColor(),
                user.getOrganization().getBackgroundColor(),
                user.getOrganization().getCardTextColor(),
                user.getOrganization().getId()
            );
        }
    }
}