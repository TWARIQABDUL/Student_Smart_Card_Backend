package com.student_smart_pay.student_management.service;

import com.student_smart_pay.student_management.models.Campus;
import com.student_smart_pay.student_management.repository.CampusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusService {

    @Autowired
    private CampusRepository campusRepository;

    // --- 1. GET ALL ---
    public List<Campus> getAllCampuses() {
        return campusRepository.findAll();
    }

    // --- 2. GET BY ID ---
    public Campus getCampusById(Long id) {
        return campusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campus not found with ID: " + id));
    }

    // --- 3. CREATE ---
    public Campus createCampus(Campus campus) {
        if (campusRepository.findByName(campus.getName()).isPresent()) {
            throw new IllegalArgumentException("Campus with this name already exists!");
        }
        return campusRepository.save(campus);
    }

    // --- 4. UPDATE ---
    public Campus updateCampus(Long id, Campus updatedDetails) {
        Campus existingCampus = getCampusById(id);

        existingCampus.setName(updatedDetails.getName());
        existingCampus.setLogoUrl(updatedDetails.getLogoUrl());
        
        // Update Theme Colors
        existingCampus.setPrimaryColor(updatedDetails.getPrimaryColor());
        existingCampus.setSecondaryColor(updatedDetails.getSecondaryColor());
        existingCampus.setBackgroundColor(updatedDetails.getBackgroundColor());
        existingCampus.setCardTextColor(updatedDetails.getCardTextColor());

        return campusRepository.save(existingCampus);
    }

    // --- 5. DELETE ---
    public void deleteCampus(Long id) {
        if (!campusRepository.existsById(id)) {
            throw new IllegalArgumentException("Campus not found");
        }
        campusRepository.deleteById(id);
    }
}