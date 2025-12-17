package com.student_smart_pay.student_management.repository;

import com.student_smart_pay.student_management.dto.Status;
import com.student_smart_pay.student_management.models.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    
    // 1. Find all logs for a specific student
    List<AccessLog> findByStudentId(Long studentId);

    // 2. Find all logs for a specific Gate
    List<AccessLog> findByGateId(String gateId);

    // 3. Find logs by status
    List<AccessLog> findByStatus(Status status);

    // --- NEW: FOR HISTORY FILTERING ---
    // Finds logs between two dates and orders them newest -> oldest.
    // The 'Pageable' parameter handles the "Limit 50" logic automatically.
    Page<AccessLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    Page<AccessLog> findByStudentIdAndTimestampBetweenOrderByTimestampDesc(
        Long studentId, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
}