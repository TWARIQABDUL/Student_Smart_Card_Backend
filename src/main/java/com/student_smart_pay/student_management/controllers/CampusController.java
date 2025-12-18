package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.StudentRepository;
import com.student_smart_pay.student_management.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/campuses")
public class CampusController {

    @Autowired
    private CampusService campusService;

    @Autowired
    private StudentRepository studentRepository;

    // --- HELPER: Identify Who is Knocking ---
    private Student getAuthenticatedUser(UserDetails userDetails) {
        return studentRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    // =========================================================================
    // 1. GET ALL CAMPUSES (Super Admin Only)
    // =========================================================================
    @GetMapping
    public ResponseEntity<?> getAllCampuses(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Student requester = getAuthenticatedUser(userDetails);

            if (requester.getRole() != Roles.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access Denied: Only Super Admin can view all campuses."));
            }

            return ResponseEntity.ok(campusService.getAllCampuses());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Server Error"));
        }
    }

    // =========================================================================
    // 2. GET SINGLE CAMPUS (Super Admin OR Owner Admin)
    // =========================================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getCampusById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        try {
            Student requester = getAuthenticatedUser(userDetails);

            // SECURITY CHECK
            boolean isSuper = requester.getRole() == Roles.SUPER_ADMIN;
            boolean isOwner = requester.getRole() == Roles.CAMPUS_ADMIN 
                           && requester.getCampus() != null 
                           && requester.getCampus().getId().equals(id);

            if (!isSuper && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access Denied: You can only view your own campus."));
            }

            return ResponseEntity.ok(campusService.getCampusById(id));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // 3. CREATE CAMPUS (Super Admin Only)
    // =========================================================================
    @PostMapping
    public ResponseEntity<?> createCampus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Campus campus
    ) {
        try {
            Student requester = getAuthenticatedUser(userDetails);

            if (requester.getRole() != Roles.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access Denied: Only Super Admin can create campuses."));
            }

            Campus newCampus = campusService.createCampus(campus);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCampus);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // 4. UPDATE CAMPUS (Super Admin OR Owner Admin)
    // =========================================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Campus campusDetails
    ) {
        try {
            Student requester = getAuthenticatedUser(userDetails);

            // SECURITY CHECK
            boolean isSuper = requester.getRole() == Roles.SUPER_ADMIN;
            boolean isOwner = requester.getRole() == Roles.CAMPUS_ADMIN 
                           && requester.getCampus() != null 
                           && requester.getCampus().getId().equals(id);

            if (!isSuper && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access Denied: You can only update your own campus."));
            }

            Campus updatedCampus = campusService.updateCampus(id, campusDetails);
            return ResponseEntity.ok(updatedCampus);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // 5. DELETE CAMPUS (Super Admin Only)
    // =========================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCampus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        try {
            Student requester = getAuthenticatedUser(userDetails);

            if (requester.getRole() != Roles.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access Denied: Only Super Admin can delete campuses."));
            }

            campusService.deleteCampus(id);
            return ResponseEntity.ok(Map.of("message", "Campus deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}