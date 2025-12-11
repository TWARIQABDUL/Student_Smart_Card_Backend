package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Deducts amount from the student's wallet identified by the NFC token.
     * Returns the updated Student object if successful.
     * Throws Exceptions if something goes wrong (which the Controller will catch).
     */
    @Transactional
    public Student processPayment(String nfcToken, BigDecimal amount) throws Exception {
        // 1. Find Student
        Optional<Student> studentOpt = studentRepository.findByNfcToken(nfcToken);
        if (studentOpt.isEmpty()) {
            throw new Exception("Error: Card not found in system.");
        }
        Student student = studentOpt.get();

        // 2. Security Checks
        if (!student.isActive()) {
            throw new Exception("Error: Student card is blocked.");
        }

        // 3. Balance Check
        if (student.getWalletBalance().compareTo(amount) < 0) {
            throw new Exception("Error: Insufficient Funds. Current Balance: " + student.getWalletBalance());
        }

        // 4. Deduct & Save
        BigDecimal newBalance = student.getWalletBalance().subtract(amount);
        student.setWalletBalance(newBalance);
        
        return studentRepository.save(student);
    }
}