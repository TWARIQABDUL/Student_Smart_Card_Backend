package com.student_smart_pay.student_management.repository;

import com.student_smart_pay.student_management.models.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    // Find a campus by name (e.g., to check duplicates)
    Optional<Campus> findByName(String name);
    Optional<Campus> findByPackageId(String packageId);
}