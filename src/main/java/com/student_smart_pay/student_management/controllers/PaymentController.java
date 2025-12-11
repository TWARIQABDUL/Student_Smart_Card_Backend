package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private StudentService studentService;

    // DTO: Defines what JSON the mobile app should send
    public record PaymentRequest(String nfcToken, BigDecimal amount) {}

    @PostMapping("/deduct")
    public ResponseEntity<?> deductMoney(@RequestBody PaymentRequest request) {
        try {
            // Call the Service to do the hard work
            Student updatedStudent = studentService.processPayment(request.nfcToken, request.amount);
            
            // Return success
            return ResponseEntity.ok("Payment Successful! New Balance: " + updatedStudent.getWalletBalance());
            
        } catch (Exception e) {
            // If the Service throws an error (e.g., "Insufficient Funds"), send it to the mobile app
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}