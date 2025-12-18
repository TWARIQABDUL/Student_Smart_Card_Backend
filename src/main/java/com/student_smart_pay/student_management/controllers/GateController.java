package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.GateVerifyRequestDto;
import com.student_smart_pay.student_management.dto.Status;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
import com.student_smart_pay.student_management.service.GateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gate")
public class GateController {

    @Autowired
    private GateService gateService;

    @Autowired
    private StudentRepository studentRepository;

    // --- HELPER: Retrieve Authenticated User (Guard or Admin) ---
    private Student getAuthenticatedUser(UserDetails userDetails) {
        return studentRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    // =========================================================================
    // 1. VERIFY ENTRY (SCANNER)
    // =========================================================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody GateVerifyRequestDto request
    ) {
        try {
            // 1. Identify the Guard performing the scan
            Student guard = getAuthenticatedUser(userDetails);

            // 2. Call Service with the Guard context (Enforces Campus Match)
            Map<String, Object> result = gateService.verifyEntry(guard, request);
            
            // 3. Return OK (The Service now puts "status": "DENIED" inside the map if needed)
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // Handle unexpected crashes
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", Status.DENIED,
                "error", "System Error: " + e.getMessage()
            ));
        }
    }

    // =========================================================================
    // 2. GET HISTORY (DASHBOARD)
    // =========================================================================
    // Example Call: GET /api/v1/gate/history?limit=20
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            // 1. Identify the Admin asking for history
            Student requester = getAuthenticatedUser(userDetails);

            // 2. Get SaaS-filtered history (Service checks roles)
            List<Map<String, Object>> history = gateService.getAccessHistory(requester, start, end, limit);
            
            return ResponseEntity.ok(history);

        } catch (SecurityException e) {
            // 403 Forbidden (If a normal student tries to access admin logs)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch history"));
        }
    }
}