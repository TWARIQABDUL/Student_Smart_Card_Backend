package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.dto.BuildStatusDto;
import com.student_smart_pay.student_management.repository.CampusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @Autowired
    private CampusRepository campusRepository;

    // DTO to capture the JSON sent by GitHub
    public record BuildStatusPayload(String package_id, String status, String download_url) {}

    @PostMapping("/build-status")
    public ResponseEntity<?> handleBuildCallback(@RequestBody BuildStatusPayload payload) {
        
        System.out.println("🔔 Webhook Received: " + payload.status() + " for " + payload.package_id());

        // 1. Find the Campus by Package ID
        // Note: In a real app, you might want to add 'findByPackageId' to your Repository interface for efficiency
        Optional<Campus> campusOpt = campusRepository.findAll().stream()
                .filter(c -> payload.package_id().equals(c.getPackageId()))
                .findFirst();

        if (campusOpt.isPresent()) {
            Campus campus = campusOpt.get();
            
            // 2. Map String Status to Enum
            if ("COMPLETED".equals(payload.status())) {
                campus.setBuildStatus(BuildStatusDto.SUCCESS);
                campus.setApkUrl(payload.download_url());
                System.out.println("✅ APK Ready: " + payload.download_url());
            } else {
                campus.setBuildStatus(BuildStatusDto.FAILED);
                System.err.println("❌ Build Failed for " + campus.getName());
            }
            
            campusRepository.save(campus);
        } else {
            System.err.println("⚠️ Unknown Package ID received: " + payload.package_id());
            return ResponseEntity.badRequest().body("Unknown Package ID");
        }

        return ResponseEntity.ok().build();
    }
}