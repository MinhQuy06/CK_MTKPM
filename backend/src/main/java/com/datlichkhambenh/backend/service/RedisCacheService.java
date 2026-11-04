package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.response.DoctorDto;
import com.datlichkhambenh.backend.dto.response.ScheduleDto;

import java.time.LocalDate;
import java.util.List;

public interface RedisCacheService {
    List<DoctorDto> getCachedDoctors(Long specialtyId);
    void cacheDoctors(Long specialtyId, List<DoctorDto> doctors);
    void evictDoctorCache(Long specialtyId);

    List<ScheduleDto> getCachedAvailableSlots(Long doctorId, LocalDate date);
    void cacheAvailableSlots(Long doctorId, LocalDate date, List<ScheduleDto> slots);
    void evictSlotCache(Long doctorId, LocalDate date);
}
