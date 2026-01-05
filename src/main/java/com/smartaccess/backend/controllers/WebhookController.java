package com.smartaccess.backend.controllers;

import com.smartaccess.backend.repository.OrganizationRepository;
import com.smartaccess.backend.models.Organization;
import com.smartaccess.backend.dto.BuildStatusDto;
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
    private OrganizationRepository campusRepository;

    // Read the secret from your environment variables
    @Value("${app.github.w_secret}")
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

        Organization campus = campusRepository.findByPackageId(packageId)
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