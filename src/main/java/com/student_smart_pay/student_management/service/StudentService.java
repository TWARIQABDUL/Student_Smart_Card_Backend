package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.dto.RegisterRequestDto;
import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Import
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject the encoder

    // --- ID GENERATOR (Keep existing) ---
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

    // --- REGISTER (HASHING ADDED) ---
    public Student registerUser(RegisterRequestDto studentDto) {
        // Validation
        if (studentRepository.findByEmail(studentDto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        Student student = new Student();
        student.setName(studentDto.getName());
        student.setEmail(studentDto.getEmail());
        student.setRole(studentDto.getRole());

        // --- SECURITY FIX: HASH PASSWORD ---
        // Never store raw passwords!
        String encodedPassword = passwordEncoder.encode(studentDto.getPassword());
        student.setPassword(encodedPassword);

        // Generate ID
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

    // --- AUTHENTICATE (MATCHING FIX) ---
    public Student authenticate(String email, String password) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);

        // --- SECURITY FIX: USE matches() ---
        // We cannot use .equals() because the DB has a hash, and input is plain text.
        // passwordEncoder.matches(raw, hash) handles the verification.
        if (studentOpt.isEmpty() || !passwordEncoder.matches(password, studentOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Student student = studentOpt.get();

        if (!student.isActive()) {
            throw new IllegalStateException("Account is suspended");
        }

        return student;
    }
}