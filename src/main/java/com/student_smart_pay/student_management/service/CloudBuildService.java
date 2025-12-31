package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.repository.CampusRepository;
import com.student_smart_pay.student_management.dto.BuildStatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CloudBuildService {

    @Value("app.github.token")
    private String githubToken;

    @Value("app.github.owner")
    private String repoOwner;

    @Value("app.github.repo")
    private String repoName;

    @Autowired
    private CampusRepository campusRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public void triggerBuild(Long campusId) {
        // 1. Fetch Campus Data
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new IllegalArgumentException("Campus not found"));

        if (campus.getPackageId() == null || campus.getLogoUrl() == null) {
            throw new IllegalArgumentException("Campus is missing Package ID or Logo URL");
        }

        // 2. Prepare GitHub API URL
        String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/dispatches";

        // 3. Set Headers (Auth)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        // 4. Create Payload (Matches your YAML)
        Map<String, Object> clientPayload = new HashMap<>();
        clientPayload.put("app_name", campus.getName());
        clientPayload.put("package_id", campus.getPackageId());
        clientPayload.put("logo_url", campus.getLogoUrl());

        Map<String, Object> body = new HashMap<>();
        body.put("event_type", "build-client-apk");
        body.put("client_payload", clientPayload);

        // 5. Fire Request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            restTemplate.postForEntity(url, entity, String.class);
            
            // 6. Update DB Status immediately
            campus.setBuildStatus(BuildStatusDto.IN_PROGRESS);
            campusRepository.save(campus);
            
            System.out.println("🚀 Build Triggered for: " + campus.getName());
            
        } catch (Exception e) {
            campus.setBuildStatus(BuildStatusDto.FAILED);
            campusRepository.save(campus);
            System.err.println("❌ Failed to trigger build: " + e.getMessage());
            throw new RuntimeException("Cloud Factory connection failed");
        }
    }
}