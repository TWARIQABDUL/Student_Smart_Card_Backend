package com.smartaccess.backend.config;

import com.smartaccess.backend.models.Organization;
import com.smartaccess.backend.models.User;
import com.smartaccess.backend.dto.Roles;
import com.smartaccess.backend.repository.OrganizationRepository;
import com.smartaccess.backend.repository.UserRepository;
import com.smartaccess.backend.service.CryptoService; // 👈 Import
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID; // 👈 Import

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UserRepository studentRepository,
                                   OrganizationRepository campusRepository,
                                   PasswordEncoder passwordEncoder,
                                   CryptoService cryptoService) { // 👈 Inject CryptoService
        return args -> {

            System.out.println("🔄 STARTING DATA LOAD...");

            // =============================================================
            // 1. INITIALIZE CAMPUSES
            // =============================================================

            // --- Campus A: Tech University (Blue Theme) ---
            Organization techCampus = campusRepository.findByName("Tech University")
                    .orElseGet(() -> {
                        Organization c = new Organization("Tech University", "#3D5CFF", "#2B45B5", "#0F111A");
                        c.setLogoUrl("https://img.icons8.com/color/480/university.png");
                        c.setAbrev("TECH");
                        return campusRepository.save(c);
                    });

            // --- Campus B: Red Rock College (Red Theme) ---
            Organization redCampus = campusRepository.findByName("Red Rock College")
                    .orElseGet(() -> {
                        Organization c = new Organization("Red Rock College", "#D32F2F", "#B71C1C", "#1E0505");
                        c.setLogoUrl("https://img.icons8.com/color/480/school.png");
                        c.setAbrev("RRC");
                        return campusRepository.save(c);
                    });

            System.out.println("✅ CAMPUSES LOADED: Tech Univ (Blue) & Red Rock (Red)");


            // =============================================================
            // 2. ROOT SUPER ADMIN (The SaaS Owner)
            // =============================================================
            if (studentRepository.findByEmail("root@system.com").isEmpty()) {
                User superAdmin = new User();
                superAdmin.setName("Super User");
                superAdmin.setEmail("root@system.com");
                superAdmin.setPassword(passwordEncoder.encode("root123"));
                superAdmin.setRole(Roles.SUPER_ADMIN);
                
                // 🔒 ENCRYPT NFC TOKEN
                superAdmin.setNfcToken(cryptoService.encrypt("SUP-ROOT-001"));
                
                // 🔑 GENERATE QR SECRET
                superAdmin.setQrSecret(UUID.randomUUID().toString());
                
                superAdmin.setWalletBalance(BigDecimal.ZERO);
                superAdmin.setActive(true);
                superAdmin.setFirstLogin(false);
                superAdmin.setValidUntil(LocalDateTime.now().plusYears(100));
                superAdmin.setCampus(null); // Global Access

                studentRepository.save(superAdmin);
                System.out.println("🚀 SUPER ADMIN: root@system.com / root123");
            }


            // =============================================================
            // 3. POPULATE TECH UNIVERSITY (Blue)
            // =============================================================

            // Tech Admin
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Tech Admin", "admin@tech.edu", "CAD-TECH-001", Roles.CAMPUS_ADMIN, techCampus);
            // Tech Guard
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Officer John", "guard@tech.edu", "GRD-TECH-001", Roles.GUARD, techCampus);
            // Tech Student 1
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Alice Student", "alice@tech.edu", "STU-TECH-001", Roles.STUDENT, techCampus);
            // Tech Student 2
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Bob Builder", "bob@tech.edu", "STU-TECH-002", Roles.STUDENT, techCampus);

            System.out.println("🔹 TECH UNIV USERS LOADED");


            // =============================================================
            // 4. POPULATE RED ROCK COLLEGE (Red)
            // =============================================================

            // Red Rock Admin
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Red Admin", "admin@redrock.edu", "CAD-RRC-001", Roles.CAMPUS_ADMIN, redCampus);
            // Red Rock Guard
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Officer Mike", "guard@redrock.edu", "GRD-RRC-001", Roles.GUARD, redCampus);
            // Red Rock Student 1
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Charlie Brown", "charlie@redrock.edu", "STU-RRC-001", Roles.STUDENT, redCampus);
            // Red Rock Student 2
            createDataUser(studentRepository, passwordEncoder, cryptoService, "Diana Prince", "diana@redrock.edu", "STU-RRC-002", Roles.STUDENT, redCampus);

            System.out.println("♦️ RED ROCK USERS LOADED");
            System.out.println("✅ DATA LOAD COMPLETE");
        };
    }

    // --- Helper Method to reduce repetition ---
    private void createDataUser(UserRepository repo, PasswordEncoder encoder, CryptoService cryptoService,
                                String name, String email, String nfcToken, 
                                Roles role, Organization campus) {
        if (repo.findByEmail(email).isEmpty()) {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            
            // 🔒 ENCRYPT TOKEN
            u.setNfcToken(cryptoService.encrypt(nfcToken));
            
            // 🔑 SET QR SECRET
            u.setQrSecret(UUID.randomUUID().toString());
            
            u.setWalletBalance(role == Roles.STUDENT ? new BigDecimal("100.00") : BigDecimal.ZERO);
            u.setActive(true);
            u.setFirstLogin(false);
            u.setPassword(encoder.encode("123")); // Default password: 123
            u.setRole(role);
            u.setValidUntil(LocalDateTime.now().plusYears(4));
            u.setCampus(campus);
            repo.save(u);
        }
    }
}