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
    
    // 1. Basic Finders
    List<AccessLog> findByStudentId(Long studentId);
    List<AccessLog> findByGateId(String gateId);
    List<AccessLog> findByStatus(Status status);

    // 2. GLOBAL HISTORY (For Super Admin)
    Page<AccessLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );

    // 3. STUDENT PERSONAL HISTORY (For "My Logs")
    Page<AccessLog> findByStudentIdAndTimestampBetweenOrderByTimestampDesc(
        Long studentId, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );

    // =========================================================================
    // 🚀 NEW: SAAS FILTER (For Campus Admins)
    // =========================================================================
    // This looks at AccessLog -> Student -> Campus -> ID
    Page<AccessLog> findByStudent_Campus_IdAndTimestampBetweenOrderByTimestampDesc(
        Long campusId, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
}