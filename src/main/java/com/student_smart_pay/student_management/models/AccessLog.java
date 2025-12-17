package com.student_smart_pay.student_management.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.student_smart_pay.student_management.dto.Status;

@Entity
@Data
@Table(name = "access_logs")
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELATIONSHIP: Many Logs -> One Student ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = true) // Nullable because "Unknown Card" has no student
    private Student student;

    // We keep these as backup in case the Student is deleted later
    private String snapshotName; 
    private String snapshotEmail;
    
    @Column(nullable = false)
    private String nfcToken;

    private String gateId; 
    private LocalDateTime timestamp;
    private Status status; // "ALLOWED", "DENIED"
    private String denialReason;
}