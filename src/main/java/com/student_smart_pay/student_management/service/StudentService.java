package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.dto.RegisterRequestDto;
import com.student_smart_pay.student_management.dto.Roles;
import com.student_smart_pay.student_management.models.AccessLog;
import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.AccessLogRepository;
import com.student_smart_pay.student_management.repository.CampusRepository;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID; // 👈 IMPORT UUID
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CryptoService cryptoService; // 👈 INJECT CRYPTO SERVICE

    // ... (Read Operations remain the same) ...
    public List<Student> getAllStudents(Student requester) {
        if (requester.getRole() == Roles.SUPER_ADMIN) {
            return studentRepository.findAll();
        } else if (requester.getRole() == Roles.CAMPUS_ADMIN) {
            Campus adminCampus = validateCampusAdmin(requester);
            return studentRepository.findByCampusId(adminCampus.getId());
        } else {
            throw new SecurityException("Access Denied: You do not have permission to view students.");
        }
    }

    public Student getStudentById(Student requester, Long studentId) {
        Student targetStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        validateCampusAccess(requester, targetStudent);
        return targetStudent;
    }

    // =========================================================================
    // ➕ CREATE OPERATION (Admin Only + Encryption)
    // =========================================================================
    @Transactional
    public Student createStudent(Student requester, RegisterRequestDto dto) {
        // 1. Permission Check
        if (requester.getRole() != Roles.SUPER_ADMIN && requester.getRole() != Roles.CAMPUS_ADMIN) {
            throw new SecurityException("Access Denied: Only Admins can create users.");
        }

        // 2. Validate Duplicates
        if (studentRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use.");
        }

        // 3. Resolve Campus
        Campus targetCampus = resolveTargetCampus(requester, dto.getCampusId());

        // 4. Build User
        Student newUser = new Student();
        newUser.setName(dto.getName());
        newUser.setEmail(dto.getEmail());
        newUser.setRole(dto.getRole() != null ? dto.getRole() : Roles.STUDENT);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setActive(true);
        newUser.setFirstLogin(true);
        newUser.setCampus(targetCampus);
        newUser.setWalletBalance(BigDecimal.ZERO);
        
        // 5. 🔒 SECURITY GENERATION
        // A. Generate the Raw ID
        String rawSmartId = generateSmartId(newUser.getRole(), targetCampus);
        
        // B. Encrypt it (So the database and physical card hold ciphertext)
        newUser.setNfcToken(cryptoService.encrypt(rawSmartId));

        // C. Generate QR Secret (For the mobile app)
        newUser.setQrSecret(UUID.randomUUID().toString());

        // 6. Set Validity
        if (newUser.getRole() == Roles.STUDENT) {
            newUser.setValidUntil(LocalDateTime.now().plusYears(4));
        } else if (newUser.getRole() == Roles.GUEST) {
            newUser.setValidUntil(LocalDateTime.now().plusDays(7));
        } else {
            newUser.setValidUntil(LocalDateTime.now().plusYears(1));
        }

        return studentRepository.save(newUser);
    }

    // ... (Update, Delete, Logs, Helpers remain the same) ...

    @Transactional
    public Student updateStudent(Student requester, Long studentId, RegisterRequestDto updates) {
        Student existingStudent = getStudentById(requester, studentId);
        if (updates.getName() != null && !updates.getName().isBlank()) existingStudent.setName(updates.getName());
        if (updates.getEmail() != null && !updates.getEmail().equals(existingStudent.getEmail())) {
            if (studentRepository.findByEmail(updates.getEmail()).isPresent()) throw new IllegalArgumentException("Email already taken.");
            existingStudent.setEmail(updates.getEmail());
        }
        if (requester.getRole() == Roles.SUPER_ADMIN && updates.getCampusId() != null) {
            Campus newCampus = campusRepository.findById(updates.getCampusId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Campus ID"));
            existingStudent.setCampus(newCampus);
        }
        return studentRepository.save(existingStudent);
    }

    @Transactional
    public void deleteStudent(Student requester, Long studentId) {
        Student existingStudent = getStudentById(requester, studentId);
        if (existingStudent.getId().equals(requester.getId())) throw new IllegalArgumentException("You cannot delete your own account.");
        studentRepository.delete(existingStudent);
    }

    public List<Map<String, Object>> getMyLogs(Student requester, LocalDateTime start, LocalDateTime end, int limit) {
        if (end == null) end = LocalDateTime.now();
        if (start == null) start = end.minusDays(30);
        Pageable pageRequest = PageRequest.of(0, limit);
        Page<AccessLog> logs = accessLogRepository.findByStudentIdAndTimestampBetweenOrderByTimestampDesc(
                requester.getId(), start, end, pageRequest
        );
        return logs.getContent().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("time", log.getTimestamp());
            map.put("gate", log.getGateId());
            map.put("status", log.getStatus());
            map.put("reason", log.getDenialReason());
            return map;
        }).collect(Collectors.toList());
    }

    // --- HELPERS ---
    private String generateSmartId(Roles role, Campus campus) {
        String prefix = switch (role) {
            case STUDENT -> "STU";
            case GUARD -> "GRD";
            case CAMPUS_ADMIN -> "CAD";
            case SUPER_ADMIN -> "SUP";
            default -> "USR";
        };
        String campAbrev = (campus != null && campus.getAbrev() != null) ? campus.getAbrev().toUpperCase() : "UNIV";
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String timeComponent = now.format(DateTimeFormatter.ofPattern("MMddHHmmssSSS"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(10, 99);
        return String.format("%s-%s-%s-%s%d", prefix, campAbrev, year, timeComponent, randomSuffix);
    }

    private Campus validateCampusAdmin(Student admin) {
        if (admin.getCampus() == null) throw new IllegalStateException("System Error: Campus Admin has no assigned campus.");
        return admin.getCampus();
    }

    private void validateCampusAccess(Student requester, Student target) {
        if (requester.getRole() == Roles.SUPER_ADMIN) return;
        if (requester.getRole() == Roles.CAMPUS_ADMIN) {
            Campus adminCampus = validateCampusAdmin(requester);
            if (!adminCampus.getId().equals(target.getCampus().getId())) throw new SecurityException("Access Denied: This student belongs to a different campus.");
            return;
        }
        if (!requester.getId().equals(target.getId())) throw new SecurityException("Access Denied.");
    }

    private Campus resolveTargetCampus(Student requester, Long requestedCampusId) {
        if (requester.getRole() == Roles.CAMPUS_ADMIN) return requester.getCampus();
        if (requestedCampusId == null) throw new IllegalArgumentException("Super Admin must specify a Campus ID.");
        return campusRepository.findById(requestedCampusId).orElseThrow(() -> new IllegalArgumentException("Invalid Campus ID"));
    }
}