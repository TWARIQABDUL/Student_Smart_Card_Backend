package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.dto.GateVerifyRequestDto;
import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.dto.Status;
import com.student_smart_pay.student_management.models.AccessLog;
import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.AccessLogRepository;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GateService {

    private static final Logger logger = LoggerFactory.getLogger(GateService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    // =========================================================================
    // 1. VERIFY ENTRY (SCANNER LOGIC)
    // =========================================================================
    public Map<String, Object> verifyEntry(Student guard, GateVerifyRequestDto request) {
        
        String token = request.getNfcToken();
        
        // Safety Check
        if (token == null || token.isBlank()) {
             return buildResponse(Status.DENIED, "Unknown", "Unknown", "Empty Token");
        }

        Optional<Student> studentOpt = Optional.empty();
        boolean isQrScan = false;

        // ---------------------------------------------------------------------
        // STEP A: TRY STATIC NFC (Exact Match)
        // ---------------------------------------------------------------------
        studentOpt = studentRepository.findByNfcToken(token);

        // ---------------------------------------------------------------------
        // STEP B: TRY DYNAMIC QR (Signature Verify)
        // ---------------------------------------------------------------------
        // Format: "STUDENT_ID:TIMESTAMP:SIGNATURE"
        if (studentOpt.isEmpty() && token.contains(":")) {
            try {
                String[] parts = token.split(":");
                if (parts.length == 3) {
                    Long id = Long.parseLong(parts[0]);
                    long timestamp = Long.parseLong(parts[1]);
                    String signature = parts[2];

                    Optional<Student> candidate = studentRepository.findById(id);
                    
                    if (candidate.isPresent()) {
                        String secret = candidate.get().getQrSecret();
                        
                        // Verify Time (30s window)
                        long now = System.currentTimeMillis();
                        if (Math.abs(now - timestamp) < 30000) {
                            
                            // Verify Signature
                            String payload = id + ":" + timestamp;
                            String expectedSig = hmacSha256(payload, secret);
                            
                            if (expectedSig.equals(signature)) {
                                studentOpt = candidate;
                                isQrScan = true;
                            } else {
                                logger.warn("⚠️ Invalid Signature for User ID: {}", id);
                            }
                        } else {
                            logger.warn("⚠️ QR Expired. Delta: {} ms", (now - timestamp));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("⚠️ Invalid QR Format: {}", e.getMessage());
            }
        }

        // ---------------------------------------------------------------------
        // STEP C: LOGGING & DECISION
        // ---------------------------------------------------------------------
        AccessLog log = new AccessLog();
        log.setNfcToken(isQrScan ? "DYNAMIC-QR" : token);
        log.setGateId(request.getGateId());
        log.setTimestamp(LocalDateTime.now());

        // 1. Invalid Token?
        if (studentOpt.isEmpty()) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("INVALID_TOKEN");
            accessLogRepository.save(log);
            return buildResponse(Status.DENIED, "Unknown", "Unknown", "Invalid or Expired Token");
        }

        Student student = studentOpt.get();
        log.setStudent(student); 
        log.setSnapshotName(student.getName());
        log.setSnapshotEmail(student.getEmail());

        // 2. SaaS Security: Wrong Campus?
        if (guard.getCampus() != null && student.getCampus() != null) {
            if (!guard.getCampus().getId().equals(student.getCampus().getId())) {
                log.setStatus(Status.DENIED);
                log.setDenialReason("WRONG_CAMPUS");
                accessLogRepository.save(log);
                logger.warn("🚨 Cross-Campus Access Attempt: {} -> {}", student.getCampus().getName(), guard.getCampus().getName());
                return buildResponse(Status.DENIED, student.getName(), student.getRole().name(), "Restricted: Wrong Campus");
            }
        }

        // 3. Account Suspended?
        if (!student.isActive()) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("SUSPENDED");
            accessLogRepository.save(log);
            return buildResponse(Status.DENIED, student.getName(), student.getRole().name(), "Account Suspended");
        }

        // 4. Card Expired?
        if (student.getValidUntil().isBefore(LocalDateTime.now())) {
            log.setStatus(Status.DENIED);
            log.setDenialReason("EXPIRED");
            accessLogRepository.save(log);
            return buildResponse(Status.DENIED, student.getName(), student.getRole().name(), "Card Expired");
        }

        // 5. SUCCESS
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

        if (requester.getRole() == Roles.SUPER_ADMIN) {
            logs = accessLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageRequest);
        } 
        else if (requester.getRole() == Roles.CAMPUS_ADMIN) {
            Campus c = requester.getCampus();
            if (c == null) throw new IllegalStateException("Admin has no campus");
            logs = accessLogRepository.findByStudent_Campus_IdAndTimestampBetweenOrderByTimestampDesc(
                c.getId(), start, end, pageRequest
            );
        } 
        else {
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
            
            if (log.getStudent() != null && log.getStudent().getCampus() != null) {
                map.put("campus", log.getStudent().getCampus().getName());
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    // --- CRYPTO HELPER ---
    private String hmacSha256(String data, String secret) {
        try {
            if (secret == null) return "";
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC Error");
        }
    }

    private Map<String, Object> buildResponse(Status status, String name, String role, String message) {
        return Map.of(
            "status", status,
            "studentName", name,
            "role", role,
            "message", message
        );
    }
}