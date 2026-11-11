package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho bảng SPECIALTIES.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
}
