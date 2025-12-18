package com.student_smart_pay.student_management.dto;

import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.dto.Roles; // Ensure this import matches your project
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private String name;
    private String email;
    private String nfcToken;      
    private Roles role;           
    private BigDecimal walletBalance;
    private LocalDateTime validUntil;
    private boolean isActive;
    
    // 👇 NEW FIELD: Stores the theme colors
    private CampusDto campus;

    // Updated Constructor
    public UserDto(String name, String email, String nfcToken, Roles role, BigDecimal walletBalance, LocalDateTime validUntil, boolean isActive, Campus campusEntity) {
        this.name = name;
        this.email = email;
        this.nfcToken = nfcToken;
        this.role = role;
        this.walletBalance = walletBalance;
        this.validUntil = validUntil;
        this.isActive = isActive;
        
        // 👇 MAP CAMPUS ENTITY TO DTO
        if (campusEntity != null) {
            this.campus = new CampusDto(
                campusEntity.getName(),
                campusEntity.getLogoUrl(),
                campusEntity.getPrimaryColor(),
                campusEntity.getSecondaryColor(),
                campusEntity.getBackgroundColor(),
                campusEntity.getCardTextColor()
            );
        }
    }
}