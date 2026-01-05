package com.smartaccess.backend.repository;

import com.smartaccess.backend.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    // Find a campus by name (e.g., to check duplicates)
    Optional<Organization> findByName(String name);
    Optional<Organization> findByPackageId(String packageId);
}