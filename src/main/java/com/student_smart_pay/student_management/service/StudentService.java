package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.models.AccessLog;
import com.student_smart_pay.student_management.models.Student;
import com.student_smart_pay.student_management.repository.AccessLogRepository;
import com.student_smart_pay.student_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    public List<Map<String, Object>> getMyLogs(LocalDateTime start, LocalDateTime end, int limit) {
        
        // 1. Get Logged-in Student
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Student not found"));

        // 2. Set Defaults (Last 30 Days if null)
        if (end == null) end = LocalDateTime.now();
        if (start == null) start = end.minusDays(30);

        // 3. Fetch Logs for THIS Student only
        Pageable pageRequest = PageRequest.of(0, limit);
        Page<AccessLog> logs = accessLogRepository.findByStudentIdAndTimestampBetweenOrderByTimestampDesc(
                student.getId(), start, end, pageRequest
        );

        // 4. Convert to JSON
        return logs.getContent().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("time", log.getTimestamp());
            map.put("gate", log.getGateId());
            map.put("status", log.getStatus());
            map.put("reason", log.getDenialReason());
            return map;
        }).collect(Collectors.toList());
    }
}