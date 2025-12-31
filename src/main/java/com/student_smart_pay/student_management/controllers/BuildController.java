package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
import com.student_smart_pay.student_management.service.CloudBuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/builds")
public class BuildController {

    @Autowired
    private CloudBuildService cloudBuildService;

    @Autowired
    private StudentRepository studentRepository;

    // Endpoint: POST /api/v1/builds/{campusId}
    @PostMapping("/{campusId}")
    public ResponseEntity<?> startBuild(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long campusId
    ) {
        // 1. Who is pressing the button?
        Student requester = studentRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // 2. Security: Only Super Admin OR the Owner of that Campus
        boolean isSuper = requester.getRole() == Roles.SUPER_ADMIN;
        boolean isOwner = requester.getRole() == Roles.CAMPUS_ADMIN 
                       && requester.getCampus() != null 
                       && requester.getCampus().getId().equals(campusId);

        if (!isSuper && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: You cannot trigger builds for this campus."));
        }

        try {
            // 3. Press the Button!
            cloudBuildService.triggerBuild(campusId);
            return ResponseEntity.ok(Map.of("message", "Build started! The APK will be ready in ~5 minutes."));
            
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Factory Error: " + e.getMessage()));
        }
    }
}