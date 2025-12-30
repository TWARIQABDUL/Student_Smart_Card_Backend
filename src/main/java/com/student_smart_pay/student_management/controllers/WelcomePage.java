package com.student_smart_pay.student_management.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class WelcomePage {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> welcome() {
        return ResponseEntity.status(200).body(
            Map.of("message", "Welcome to the Student Management System!")
        );
    }
}
