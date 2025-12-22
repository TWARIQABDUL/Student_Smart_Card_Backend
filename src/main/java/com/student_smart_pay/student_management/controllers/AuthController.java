package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.FirstLoginChangePasswordDto; 
import com.student_smart_pay.student_management.dto.LoginRequestDto;
import com.student_smart_pay.student_management.dto.LoginResponseDto;
import com.student_smart_pay.student_management.dto.RegisterRequestDto;
import com.student_smart_pay.student_management.dto.UserDto;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.service.JwtService;
import com.student_smart_pay.student_management.service.AuthServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @Autowired
    private AuthServices studentService;

    @Autowired
    private JwtService jwtService;

    // --- HELPER: Convert Entity to DTO ---
    // private UserDto mapToUserDto(Student student) {
    //     // 👇 FIX: Added student.getCampus() at the end
    //     return new UserDto(
    //         student.getName(),
    //         student.getEmail(),
    //         student.getNfcToken(),
    //         student.getRole(),
    //         student.getWalletBalance(),
    //         student.getValidUntil(),
    //         student.isActive(),
    //         student.getCampus() 
    //     );
    // }

    private UserDto mapToUserDto(Student student) {
        return new UserDto(student);
    }

    // --- REGISTER ---
    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDto studentDto) {
        try {
            Student registeredStudent = studentService.registerUser(studentDto);
            UserDto safeUser = mapToUserDto(registeredStudent);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "User registered successfully", "user", safeUser)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // --- LOGIN ---
    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginDto) {
        try {
            // 1. Authenticate (Checks DB)
            Student student = studentService.authenticate(loginDto.getEmail(), loginDto.getPassword());

            // 2. CHECK: IS IT FIRST LOGIN?
            if (student.isFirstLogin()) {
                return ResponseEntity.ok(Map.of(
                    "status", "FORCE_CHANGE_PASSWORD",
                    "message", "First time login. Please change your password.",
                    "email", student.getEmail()
                ));
            }

            // 3. If NOT first login, generate Token
            String token = jwtService.generateToken(student.getEmail(), student.getRole().name());
            UserDto safeUser = mapToUserDto(student);

            // 4. Return Structured Response
            return ResponseEntity.ok(new LoginResponseDto(token, "Login successful", safeUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Login failed"));
        }
    }

    // --- FORCE CHANGE PASSWORD ENDPOINT ---
    @PostMapping("/auth/change-first-password")
    public ResponseEntity<?> changeFirstPassword(@RequestBody FirstLoginChangePasswordDto request) {
        try {
            // 1. Call Service to update password & disable flag
            Student updatedStudent = studentService.changePassword(
                request.getEmail(), 
                request.getOldPassword(), 
                request.getNewPassword()
            );

            // 2. Generate Token immediately so user is logged in
            String token = jwtService.generateToken(updatedStudent.getEmail(), updatedStudent.getRole().name());
            UserDto safeUser = mapToUserDto(updatedStudent);

            return ResponseEntity.ok(new LoginResponseDto(token, "Password changed successfully", safeUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Password change failed"));
        }
    }
}