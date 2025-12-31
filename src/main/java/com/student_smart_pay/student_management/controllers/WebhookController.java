package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.repository.CampusRepository;
import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.dto.BuildStatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @Autowired
    private CampusRepository campusRepository;

    // Read the secret from your environment variables
    @Value("${app.webhook.secret}")
    private String expectedSecret;

    @PostMapping("/build-status")
    public ResponseEntity<?> updateBuildStatus(
            @RequestHeader(value = "X-Electron-Secret", required = false) String secretKey, // 👈 CHECK HEADER
            @RequestBody Map<String, String> payload
    ) {
        // 1. SECURITY CHECK 🛡️
        if (secretKey == null || !secretKey.equals(expectedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("⛔ Invalid Secret Key");
        }

        String status = payload.get("status");
        String downloadUrl = payload.get("download_url");
        String packageId = payload.get("package_id");

        System.out.println("🔔 Webhook Received for: " + packageId + " [" + status + "]");

        Campus campus = campusRepository.findByPackageId(packageId)
                .orElseThrow(() -> new RuntimeException("Campus not found"));

        if ("COMPLETED".equals(status)) {
            campus.setBuildStatus(BuildStatusDto.SUCCESS);
            campus.setApkUrl(downloadUrl);
        } else {
            campus.setBuildStatus(BuildStatusDto.FAILED);
        }

        campusRepository.save(campus);
        return ResponseEntity.ok("Update Successful");
    }
}