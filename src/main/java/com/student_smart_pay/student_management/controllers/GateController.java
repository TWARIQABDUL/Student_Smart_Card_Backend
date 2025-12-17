package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.GateVerifyRequestDto;
import com.student_smart_pay.student_management.dto.Status;
import com.student_smart_pay.student_management.service.GateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // Required for Date Params
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gate")
public class GateController {

    @Autowired
    private GateService gateService;

    // =========================================================================
    // 1. VERIFY ENTRY (SCANNER)
    // =========================================================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEntry(@RequestBody GateVerifyRequestDto request) {
        try {
            Map<String, Object> result = gateService.verifyEntry(request);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", Status.DENIED,
                "error", e.getMessage()
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", Status.DENIED,
                "error", e.getMessage()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", Status.DENIED,
                "error", "System Error: " + e.getMessage()
            ));
        }
    }

    // =========================================================================
    // 2. GET HISTORY (DASHBOARD)
    // =========================================================================
    // Example Call: GET /api/v1/gate/history?limit=20&start=2024-01-01T10:00:00
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        // The Service handles the defaults (if start/end are null)
        List<Map<String, Object>> history = gateService.getAccessHistory(start, end, limit);
        
        return ResponseEntity.ok(history);
    }
}