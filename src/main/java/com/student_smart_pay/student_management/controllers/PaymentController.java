package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.RegisterRequestDto;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.service.AuthServices;
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
        Student registeredStudent = studentService.registerUser(studentDto);
        return ResponseEntity.ok(registeredStudent);
    }
}