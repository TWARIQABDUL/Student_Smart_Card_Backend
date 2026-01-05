package com.smartaccess.backend.models;

import com.smartaccess.backend.dto.BuildStatusDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organizations") // 👈 Refactored: Table is now "organizations"
@Data
@NoArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 
    
    // e.g., "Tech University" -> "TECH", "Gold's Gym" -> "GOLD"
    private String abbreviation; // 👈 Refactored: Renamed from "abrev" for clarity

    private String logoUrl;

    // --- COLOR PALETTE (Hex Codes) ---
    private String primaryColor;   
    private String secondaryColor; 
    private String backgroundColor; 
    private String cardTextColor; 
    
    @Column(unique = true)
    private String packageId; // e.g. "com.smartaccess.gymname"
    
    private String apkUrl;    
    
    private BuildStatusDto buildStatus;

    public Organization(String name, String primaryColor, String secondaryColor, String backgroundColor) {
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.backgroundColor = backgroundColor;
        this.cardTextColor = "#FFFFFF"; 
        this.buildStatus = BuildStatusDto.NOT_STARTED; 
    }
}