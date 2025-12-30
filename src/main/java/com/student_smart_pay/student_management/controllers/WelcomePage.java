package com.student_smart_pay.student_management.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class WelcomePage {

    @GetMapping("/")
    public String welcome() {
        return "Welcome to the Student Management System!";
    }
}
