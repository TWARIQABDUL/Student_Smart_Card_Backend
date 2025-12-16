package com.student_smart_pay.student_management.config;

import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(StudentRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            // UPDATED: Use a format that matches our new system rules
            String testToken = "STU-2025-TEST-0001"; 

            if (repository.findByNfcToken(testToken).isEmpty()) {
                Student student = new Student();
                student.setName("Test Student");
                student.setEmail("test@campus.edu");
                student.setNfcToken(testToken); // Predictable ID for testing
                student.setWalletBalance(new BigDecimal("100.00"));
                student.setActive(true);

                // Passwords must be hashed
                student.setPassword(passwordEncoder.encode("password123"));

                // Valid for 4 years
                student.setRole(Roles.STUDENT);
                student.setValidUntil(LocalDateTime.now().plusYears(4));
                
                repository.save(student);
                System.out.println("✅ TEST DATA LOADED: 'STU-2025-TEST-0001' created.");
                System.out.println("👉 Login: test@campus.edu / password123");
            }
        };
    }
}