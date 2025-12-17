package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.dto.RegisterRequestDto;
import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
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
            case ADMIN -> "ADM";
            case GUEST -> "GST";
        };
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String timeComponent = now.format(DateTimeFormatter.ofPattern("MMddHHmmssSSS"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(10, 99);
        return String.format("%s-%s-%s%d", prefix, year, timeComponent, randomSuffix);
    }

    // --- REGISTER ---
    public Student registerUser(RegisterRequestDto studentDto) {
        if (studentRepository.findByEmail(studentDto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        Student student = new Student();
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
    public Student authenticate(String email, String password) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);

        if (studentOpt.isEmpty() || !passwordEncoder.matches(password, studentOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Student student = studentOpt.get();

        if (!student.isActive()) {
            throw new IllegalStateException("Account is suspended");
        }

        return student;
    }

    // --- 🚀 NEW: CHANGE PASSWORD (For First Login) ---
    public Student changePassword(String email, String oldPassword, String newPassword) {
        Student student = studentRepository.findByEmail(email)
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