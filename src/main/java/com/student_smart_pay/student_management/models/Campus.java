package com.student_smart_pay.student_management.models;
import com.student_smart_pay.student_management.dto.BuildStatusDto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campuses")
@Data
@NoArgsConstructor
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 
    private String abrev;        // e.g., "Tech University"
    private String logoUrl;        // e.g., "https://univ.edu/logo.png"

    // --- COLOR PALETTE (Hex Codes) ---
    private String primaryColor;   // e.g., "#3D5CFF" (Main Brand Color)
    private String secondaryColor; // e.g., "#2B45B5" (Gradient/Accents)
    private String backgroundColor; // e.g., "#0F111A" (App Background)
    private String cardTextColor;  // e.g., "#FFFFFF" (Text on Card)
    @Column(unique = true)
    private String packageId; // e.g. "com.tech.univ"
    
    private String apkUrl;    // e.g. "https://do-spaces.../app.apk"
    
    private BuildStatusDto buildStatus;
    public Campus(String name, String primaryColor, String secondaryColor, String backgroundColor) {
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.backgroundColor = backgroundColor;
        this.cardTextColor = "#FFFFFF"; // Default white
        this.buildStatus = BuildStatusDto.NOT_STARTED; // Default status
    }
}