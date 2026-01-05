package com.smartaccess.backend.repository;

import com.smartaccess.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Boot automatically converts this method name into a SQL query:
    // SELECT * FROM students WHERE nfc_token = ?
    Optional<User> findByNfcToken(String nfcToken);
    
    // Useful for login later
    Optional<User> findByEmail(String email);
    List<User> findByCampusId(Long campusId);
}