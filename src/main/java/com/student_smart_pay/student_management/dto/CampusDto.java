package com.student_smart_pay.student_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CampusDto {
    private String name;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String backgroundColor;
    private String cardTextColor;
}