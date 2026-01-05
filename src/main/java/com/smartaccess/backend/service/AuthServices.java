package com.smartaccess.backend.service;

import com.smartaccess.backend.dto.RegisterRequestDto;
import com.smartaccess.backend.dto.Roles;
import com.smartaccess.backend.models.User;
import com.smartaccess.backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthServices {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- ID GENERATOR ---
    private String generateSmartId(Roles role) {
        String prefix = switch (role) {
            case STUDENT -> "STU";
            case GUARD -> "GRD";
            case CAMPUS_ADMIN -> "CAD"; // Added for Campus Admin
            case SUPER_ADMIN -> "SUP";
            case GUEST -> "GST";
        };
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String timeComponent = now.format(DateTimeFormatter.ofPattern("MMddHHmmssSSS"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(10, 99);
        return String.format("%s-%s-%s%d", prefix, year, timeComponent, randomSuffix);
    }

    // --- REGISTER ---
    public User registerUser(RegisterRequestDto studentDto) {
        if (studentRepository.findByEmail(studentDto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        User student = new User();
        student.setName(studentDto.getName());
        student.setEmail(studentDto.getEmail());
        student.setRole(studentDto.getRole());

        // Hash Password
        String encodedPassword = passwordEncoder.encode(studentDto.getPassword());
        student.setPassword(encodedPassword);
        
        // 🚀 SET FIRST LOGIN FLAG
        student.setFirstLogin(true);

        // Generate ID & Token
        String smartId = generateSmartId(student.getRole());
        student.setNfcToken(smartId);

        // Validity Logic
        if (student.getRole() == Roles.STUDENT) {
            student.setValidUntil(LocalDateTime.now().plusYears(4));
        } else if (student.getRole() == Roles.GUEST) {
            student.setValidUntil(LocalDateTime.now().plusDays(7));
        } else {
            student.setValidUntil(LocalDateTime.now().plusYears(1));
        }

        student.setWalletBalance(BigDecimal.ZERO);
        return studentRepository.save(student);
    }

    // --- AUTHENTICATE ---
    public User authenticate(String email, String password) {
        Optional<User> studentOpt = studentRepository.findByEmail(email);

        if (studentOpt.isEmpty() || !passwordEncoder.matches(password, studentOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User student = studentOpt.get();

        if (!student.isActive()) {
            throw new IllegalStateException("Account is suspended");
        }

        return student;
    }

    // --- 🚀 NEW: CHANGE PASSWORD (For First Login) ---
    public User changePassword(String email, String oldPassword, String newPassword) {
        User student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. Verify OLD Password
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        // 2. Hash NEW Password
        student.setPassword(passwordEncoder.encode(newPassword));

        // 3. Disable the First Login Flag
        student.setFirstLogin(false);

        return studentRepository.save(student);
    }
}