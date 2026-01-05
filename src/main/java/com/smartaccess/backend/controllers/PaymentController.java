package com.smartaccess.backend.controllers;

import com.smartaccess.backend.dto.RegisterRequestDto;
import com.smartaccess.backend.models.User;
import com.smartaccess.backend.service.AuthServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private AuthServices studentService;

    // DTO: Defines what JSON the mobile app should send
    public record PaymentRequest(String nfcToken, BigDecimal amount) {}

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDto studentDto) {
        User registeredStudent = studentService.registerUser(studentDto);
        return ResponseEntity.ok(registeredStudent);
    }
}