package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.dto.GateVerifyRequestDto;
import com.student_smart_pay.student_management.dto.Status;
import com.student_smart_pay.student_management.models.AccessLog;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.AccessLogRepository;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GateService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    // =========================================================================
    // 1. VERIFY ENTRY (SCANNER LOGIC)
    // =========================================================================
    public Map<String, Object> verifyEntry(GateVerifyRequestDto request) {
        Optional<Student> studentOpt = studentRepository.findByNfcToken(request.getNfcToken());
        
        AccessLog log = new AccessLog();
        log.setNfcToken(request.getNfcToken());
        log.setGateId(request.getGateId());
        log.setTimestamp(LocalDateTime.now());

        // A. CHECK: Does card exist?
        if (studentOpt.isEmpty()) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("INVALID_CARD");
            accessLogRepository.save(log); 
            throw new IllegalArgumentException("Unknown Card Token");
        }

        Student student = studentOpt.get();
        
        // --- 🔗 SET RELATIONSHIP ---
        log.setStudent(student); 
        log.setSnapshotName(student.getName());
        log.setSnapshotEmail(student.getEmail());

        // B. CHECK: Is account suspended?
        if (!student.isActive()) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("ACCOUNT_SUSPENDED");
            accessLogRepository.save(log);
            throw new IllegalStateException("Student Account is Suspended");
        }

        // C. CHECK: Is card expired?
        if (student.getValidUntil().isBefore(LocalDateTime.now())) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("CARD_EXPIRED");
            accessLogRepository.save(log);
            throw new IllegalStateException("Card Expired");
        }

        // D. SUCCESS
        log.setStatus(Status.ALLOWED);
        accessLogRepository.save(log);

        return Map.of(
            "status", Status.ALLOWED,
            "studentName", student.getName(),
            "role", student.getRole(),
            "message", "Access Granted"
        );
    }

    // =========================================================================
    // 2. GET HISTORY (DASHBOARD LOGIC)
    // =========================================================================
    public List<Map<String, Object>> getAccessHistory(LocalDateTime start, LocalDateTime end, int limit) {
        
        // A. Defaults: If no dates provided, show last 7 days
        if (end == null) end = LocalDateTime.now();
        if (start == null) start = end.minusDays(7); 

        // B. Pagination: Limit the results (e.g. Top 50)
        Pageable pageRequest = PageRequest.of(0, limit);

        // C. Query DB using the method we added to AccessLogRepository
        Page<AccessLog> logs = accessLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageRequest);

        // D. Convert to JSON-friendly Map
        return logs.getContent().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            // Use snapshot name in case student was deleted later
            map.put("studentName", log.getSnapshotName() != null ? log.getSnapshotName() : "Unknown");
            map.put("nfcToken", log.getNfcToken());
            map.put("status", log.getStatus());
            map.put("reason", log.getDenialReason());
            map.put("time", log.getTimestamp());
            map.put("gateId", log.getGateId());
            return map;
        }).collect(Collectors.toList());
    }
}