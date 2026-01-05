package com.smartaccess.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrganizationDto {
    private String name;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String backgroundColor;
    private String cardTextColor;
    
    // 👈 Refactored: Renamed from campusId
    private Long organizationId; 
}