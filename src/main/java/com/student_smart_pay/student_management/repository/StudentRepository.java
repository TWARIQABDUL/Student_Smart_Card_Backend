package com.student_smart_pay.student_management.repository;

import com.student_smart_pay.student_management.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Spring Boot automatically converts this method name into a SQL query:
    // SELECT * FROM students WHERE nfc_token = ?
    Optional<Student> findByNfcToken(String nfcToken);
    
    // Useful for login later
    Optional<Student> findByEmail(String email);
}