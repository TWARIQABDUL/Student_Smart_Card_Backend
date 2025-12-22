package com.student_smart_pay.student_management.controllers;

import com.student_smart_pay.student_management.dto.RegisterRequestDto;
import com.student_smart_pay.student_management.dto.UserDto;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.service.StudentService;
import com.student_smart_pay.student_management.repository.StudentRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    // --- HELPER: Retrieve Authenticated User from DB ---
    private Student getAuthenticatedStudent(UserDetails userDetails) {
        return studentRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
    }

    // --- HELPER: Convert Entity to DTO ---
    // private UserDto mapToDto(Student student) {
    //     return new UserDto(
    //         student.getName(),
    //         student.getEmail(),
    //         student.getNfcToken(),
    //         student.getRole(),
    //         student.getWalletBalance(),
    //         student.getValidUntil(),
    //         student.isActive(),
    //         student.getCampus()
    //     );
    // }
    private UserDto mapToDto(Student student) {
        return new UserDto(student);
    }

    // =========================================================================
    // 1. GET ALL STUDENTS
    // =========================================================================
    @GetMapping
    public ResponseEntity<?> getAllStudents(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Student requester = getAuthenticatedStudent(userDetails);
            List<Student> students = studentService.getAllStudents(requester);
            
            // Map to DTOs
            List<UserDto> dtos = students.stream().map(this::mapToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    // =========================================================================
    // 2. GET SINGLE STUDENT
    // =========================================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        try {
            Student requester = getAuthenticatedStudent(userDetails);
            Student target = studentService.getStudentById(requester, id);
            return ResponseEntity.ok(mapToDto(target));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // 3. CREATE STUDENT (Admin Only)
    // =========================================================================
    @PostMapping
    public ResponseEntity<?> createStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody RegisterRequestDto request
    ) {
        try {
            Student requester = getAuthenticatedStudent(userDetails);
            Student createdStudent = studentService.createStudent(requester, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(createdStudent));

        } catch (SecurityException e) {
            // 403: Not allowed (Student trying to create Admin)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 400: Bad Request (Duplicate Email, Missing ID)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create student"));
        }
    }

    // =========================================================================
    // 4. UPDATE STUDENT
    // =========================================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody RegisterRequestDto request
    ) {
        try {
            Student requester = getAuthenticatedStudent(userDetails);
            Student updatedStudent = studentService.updateStudent(requester, id, request);
            return ResponseEntity.ok(mapToDto(updatedStudent));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // 5. DELETE STUDENT
    // =========================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        try {
            Student requester = getAuthenticatedStudent(userDetails);
            studentService.deleteStudent(requester, id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // Can happen if ID is not found or trying to delete self
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // 6. HISTORY (Access Logs)
    // =========================================================================
    @GetMapping("/history")
    public ResponseEntity<?> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            Student requester = getAuthenticatedStudent(userDetails);
            var logs = studentService.getMyLogs(requester, start, end, limit);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch logs"));
        }
    }
}