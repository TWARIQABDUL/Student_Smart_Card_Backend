package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    // GET /api/student/{token}
    @GetMapping("/{token}")
    public ResponseEntity<?> getStudent(@PathVariable String token) {
        // 1. Find the student in the DB
        Optional<Student> studentOpt = studentRepository.findByNfcToken(token);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 if token doesn't exist
        }

        // 2. Return the student object (includes name & walletBalance)
        return ResponseEntity.ok(studentOpt.get());
    }
}