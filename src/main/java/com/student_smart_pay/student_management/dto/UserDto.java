package com.student_smart_pay.student_management.dto;

import com.student_smart_pay.student_management.models.Student; // 👈 Import Student
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id; // Useful for the frontend to know the ID
    private String name;
    private String email;
    private String nfcToken;      
    private Roles role;           
    private BigDecimal walletBalance;
    private LocalDateTime validUntil;
    private boolean isActive;
    
    // 🔑 NEW FIELD: Send the QR Secret to the Mobile App
    private String qrSecret; 
    
    // Theme colors
    private CampusDto campus;

    // ✅ CLEANER CONSTRUCTOR: Takes the Student Entity directly
    public UserDto(Student student) {
        this.id = student.getId();
        this.name = student.getName();
        this.email = student.getEmail();
        this.nfcToken = student.getNfcToken();
        this.role = student.getRole();
        this.walletBalance = student.getWalletBalance();
        this.validUntil = student.getValidUntil();
        this.isActive = student.isActive();
        
        // 🚀 Pass the secret so the App can generate QR codes
        this.qrSecret = student.getQrSecret(); 

        // Map Campus if it exists
        if (student.getCampus() != null) {
            this.campus = new CampusDto(
                student.getCampus().getName(),
                student.getCampus().getLogoUrl(),
                student.getCampus().getPrimaryColor(),
                student.getCampus().getSecondaryColor(),
                student.getCampus().getBackgroundColor(),
                student.getCampus().getCardTextColor(),
                student.getCampus().getId()
            );
        }
    }
}