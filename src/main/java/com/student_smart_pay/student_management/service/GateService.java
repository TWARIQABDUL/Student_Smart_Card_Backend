package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.dto.GateVerifyRequestDto;
import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.dto.Status;
import com.student_smart_pay.student_management.models.AccessLog;
import com.student_smart_pay.student_management.models.Campus;
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
    // ⚠️ Updated to accept the 'guard' performing the scan
    public Map<String, Object> verifyEntry(Student guard, GateVerifyRequestDto request) {
        
        Optional<Student> studentOpt = studentRepository.findByNfcToken(request.getNfcToken());
        
        AccessLog log = new AccessLog();
        log.setNfcToken(request.getNfcToken());
        log.setGateId(request.getGateId());
        log.setTimestamp(LocalDateTime.now());

        // A. CHECK: DOES CARD EXIST?
        if (studentOpt.isEmpty()) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("INVALID_CARD");
            accessLogRepository.save(log); 
            // We return a polite map instead of crashing, so the App shows "Red Screen"
            return buildResponse(Status.DENIED, "Unknown", "Unknown Card", "INVALID_CARD");
        }

        Student student = studentOpt.get();
        log.setStudent(student); 
        log.setSnapshotName(student.getName());
        log.setSnapshotEmail(student.getEmail());

        // B. 🚀 SECURITY CHECK: CROSS-CAMPUS SCAN
        // The Guard's Campus MUST match the Student's Campus
        if (guard.getCampus() != null && !guard.getCampus().getId().equals(student.getCampus().getId())) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("WRONG_CAMPUS");
            accessLogRepository.save(log);
            return buildResponse(Status.DENIED, student.getName(), student.getRole().name(), "Restricted: Wrong Campus");
        }

        // C. CHECK: ACCOUNT SUSPENDED?
        if (!student.isActive()) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("SUSPENDED");
            accessLogRepository.save(log);
            return buildResponse(Status.DENIED, student.getName(), student.getRole().name(), "Account Suspended");
        }

        // D. CHECK: CARD EXPIRED?
        if (student.getValidUntil().isBefore(LocalDateTime.now())) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("EXPIRED");
            accessLogRepository.save(log);
            return buildResponse(Status.DENIED, student.getName(), student.getRole().name(), "Card Expired");
        }

        // E. SUCCESS!
        log.setStatus(Status.ALLOWED);
        accessLogRepository.save(log);

        return buildResponse(Status.ALLOWED, student.getName(), student.getRole().name(), "Access Granted");
    }

    // =========================================================================
    // 2. GET HISTORY (DASHBOARD LOGIC)
    // =========================================================================
    public List<Map<String, Object>> getAccessHistory(Student requester, LocalDateTime start, LocalDateTime end, int limit) {
        
        if (end == null) end = LocalDateTime.now();
        if (start == null) start = end.minusDays(7); 
        Pageable pageRequest = PageRequest.of(0, limit);

        Page<AccessLog> logs;

        // 🚀 LOGIC: Filter based on who is asking
        if (requester.getRole() == Roles.SUPER_ADMIN) {
            // Super Admin sees EVERYTHING
            logs = accessLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageRequest);
        } 
        else if (requester.getRole() == Roles.CAMPUS_ADMIN) {
            // Campus Admin sees ONLY their campus logs
            Campus c = requester.getCampus();
            if (c == null) throw new IllegalStateException("Admin has no campus");
            
            logs = accessLogRepository.findByStudent_Campus_IdAndTimestampBetweenOrderByTimestampDesc(
                c.getId(), start, end, pageRequest
            );
        } 
        else {
            // Guards/Students shouldn't use this endpoint for dashboarding, but safe default:
            throw new SecurityException("Access Denied: You cannot view global logs.");
        }

        return logs.getContent().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("studentName", log.getSnapshotName() != null ? log.getSnapshotName() : "Unknown");
            map.put("nfcToken", log.getNfcToken());
            map.put("status", log.getStatus());
            map.put("reason", log.getDenialReason());
            map.put("time", log.getTimestamp());
            map.put("gateId", log.getGateId());
            
            // Add Campus Name to logs (Useful for Super Admin)
            if (log.getStudent() != null && log.getStudent().getCampus() != null) {
                map.put("campus", log.getStudent().getCampus().getName());
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    // Helper to build consistent JSON response
    private Map<String, Object> buildResponse(Status status, String name, String role, String message) {
        return Map.of(
            "status", status,
            "studentName", name,
            "role", role,
            "message", message
        );
    }
}