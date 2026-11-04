package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.request.MedicalServiceRequestDto;
import com.datlichkhambenh.backend.dto.response.MedicalServiceDto;

import java.util.List;

public interface MedicalServiceService {
    List<MedicalServiceDto> getActiveServices();
    List<MedicalServiceDto> getAllServices();
    MedicalServiceDto create(MedicalServiceRequestDto request);
    MedicalServiceDto update(Long id, MedicalServiceRequestDto request);
    void disable(Long id);
}
