package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.LoginRequestDto;
import com.student_smart_pay.student_management.dto.LoginResponseDto; // Import
import com.student_smart_pay.student_management.dto.RegisterRequestDto;
import com.student_smart_pay.student_management.dto.UserDto;         // Import
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.service.JwtService;
import com.student_smart_pay.student_management.service.StudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private JwtService jwtService;

    // --- HELPER: Convert Entity to DTO ---
    private UserDto mapToUserDto(Student student) {
        return new UserDto(
            student.getName(),
            student.getEmail(),
            student.getNfcToken(),
            student.getRole(),
            student.getWalletBalance(),
            student.getValidUntil(),
            student.isActive()
        );
    }

    // --- REGISTER ---
    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDto studentDto) {
        try {
            Student registeredStudent = studentService.registerUser(studentDto);
            
            // Convert to safe DTO
            UserDto safeUser = mapToUserDto(registeredStudent);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                    "message", "User registered successfully",
                    "user", safeUser // Returns clean JSON without password
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Registration failed"));
        }
    }

    // --- LOGIN ---
    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginDto) {
        try {
            // 1. Authenticate (Checks DB)
            Student student = studentService.authenticate(loginDto.getEmail(), loginDto.getPassword());

            // 2. Generate Token
            String token = jwtService.generateToken(student.getEmail(), student.getRole().name());

            // 3. Create Safe User DTO
            UserDto safeUser = mapToUserDto(student);

            // 4. Return Structured Response
            return ResponseEntity.ok(new LoginResponseDto(token, "Login successful", safeUser));

        } catch (IllegalArgumentException e) {
            // 401 Unauthorized (Wrong password/email)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));

        } catch (IllegalStateException e) {
            // 403 Forbidden (Account suspended)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Login failed"));
        }
    }
}