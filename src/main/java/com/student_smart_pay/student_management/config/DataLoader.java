package com.student_smart_pay.student_management.config;

import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(StudentRepository repository) {
        return args -> {
            // "STUDENT-ID-12345-SECURE" must match the hardcoded string in your Flutter App
            String token = "STUDENT-ID-12345-SECURE"; 

            if (repository.findByNfcToken(token).isEmpty()) {
                Student student = new Student();
                student.setName("Abdul Aziz");
                student.setEmail("abdul@campus.edu"); // Added email since it's in your model
                student.setNfcToken(token);
                student.setWalletBalance(new BigDecimal("100.00")); // Give free money for testing
                student.setActive(true);
                
                repository.save(student);
                System.out.println("✅ TEST DATA LOADED: Student 'Abdul Aziz' created with $100.00");
            }
        };
    }
}