package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsBySpecialtyId(Long specialtyId);
    List<MedicalService> findByActiveTrueOrderByNameAsc();
}
