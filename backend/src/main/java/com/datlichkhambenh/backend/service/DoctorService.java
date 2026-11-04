package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.request.UpdateDoctorProfileDto;
import com.datlichkhambenh.backend.dto.response.DoctorDto;
import com.datlichkhambenh.backend.dto.response.SpecialtyDto;

import java.math.BigDecimal;
import java.util.List;

public interface DoctorService {
    List<DoctorDto> searchApprovedDoctors(Long specialtyId, String keyword, BigDecimal minFee,
                                           BigDecimal maxFee, Integer minExperience, String sort,
                                           int page, int size);
    DoctorDto getApprovedDoctor(Long doctorId);
    DoctorDto getProfile(Long doctorUserId);
    DoctorDto updateProfile(Long doctorUserId, UpdateDoctorProfileDto request);
    List<SpecialtyDto> getSpecialties();
}
