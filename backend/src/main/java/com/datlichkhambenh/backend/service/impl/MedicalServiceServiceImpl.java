package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.dto.request.MedicalServiceRequestDto;
import com.datlichkhambenh.backend.dto.response.MedicalServiceDto;
import com.datlichkhambenh.backend.entity.MedicalService;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.repository.MedicalServiceRepository;
import com.datlichkhambenh.backend.repository.SpecialtyRepository;
import com.datlichkhambenh.backend.service.MedicalServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalServiceServiceImpl implements MedicalServiceService {
    private final MedicalServiceRepository repository;
    private final SpecialtyRepository specialtyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicalServiceDto> getActiveServices() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalServiceDto> getAllServices() {
        return repository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional
    public MedicalServiceDto create(MedicalServiceRequestDto request) {
        if (repository.existsByCodeIgnoreCase(request.getCode())) {
            throw new AppException("Mã dịch vụ đã tồn tại");
        }
        return mapToDto(repository.save(apply(new MedicalService(), request)));
    }

    @Override
    @Transactional
    public MedicalServiceDto update(Long id, MedicalServiceRequestDto request) {
        MedicalService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));
        if (!service.getCode().equalsIgnoreCase(request.getCode())
                && repository.existsByCodeIgnoreCase(request.getCode())) {
            throw new AppException("Mã dịch vụ đã tồn tại");
        }
        return mapToDto(repository.save(apply(service, request)));
    }

    @Override
    @Transactional
    public void disable(Long id) {
        MedicalService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));
        service.setActive(false);
        repository.save(service);
    }

    private MedicalService apply(MedicalService service, MedicalServiceRequestDto request) {
        service.setCode(request.getCode().trim().toUpperCase());
        service.setName(request.getName().trim());
        service.setDescription(request.getDescription());
        service.setDefaultPrice(request.getDefaultPrice());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setActive(request.getActive() == null || request.getActive());
        service.setSpecialty(request.getSpecialtyId() == null ? null
                : specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa")));
        return service;
    }

    private MedicalServiceDto mapToDto(MedicalService service) {
        return MedicalServiceDto.builder().id(service.getId()).code(service.getCode()).name(service.getName())
                .description(service.getDescription())
                .specialtyId(service.getSpecialty() == null ? null : service.getSpecialty().getId())
                .specialtyName(service.getSpecialty() == null ? "Đa khoa" : service.getSpecialty().getName())
                .defaultPrice(service.getDefaultPrice()).durationMinutes(service.getDurationMinutes())
                .active(service.getActive()).build();
    }
}
