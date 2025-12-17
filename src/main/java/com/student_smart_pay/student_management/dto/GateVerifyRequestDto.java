package com.student_smart_pay.student_management.dto;

import lombok.Data;

@Data
public class GateVerifyRequestDto {
    private String nfcToken;
    private String gateId; // The ID of the guard's device/location
}