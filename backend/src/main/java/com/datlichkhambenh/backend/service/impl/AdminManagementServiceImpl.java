package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.dto.request.SpecialtyRequestDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.dto.response.DoctorDto;
import com.datlichkhambenh.backend.dto.response.SpecialtyDto;
import com.datlichkhambenh.backend.dto.response.UserDto;
import com.datlichkhambenh.backend.dto.response.PatientDto;
import com.datlichkhambenh.backend.dto.request.AdminCreateDoctorDto;
import com.datlichkhambenh.backend.dto.request.AdminCreatePatientDto;
import com.datlichkhambenh.backend.dto.request.UpdateDoctorProfileDto;
import com.datlichkhambenh.backend.dto.request.UpdatePatientProfileDto;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.entity.Specialty;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.repository.*;
import com.datlichkhambenh.backend.service.AdminManagementService;
import com.datlichkhambenh.backend.service.RedisCacheService;
import com.datlichkhambenh.backend.service.DoctorService;
import com.datlichkhambenh.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Locale;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AdminManagementServiceImpl implements AdminManagementService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final RedisCacheService redisCacheService;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PasswordEncoder passwordEncoder;
    private final MedicalServiceRepository medicalServiceRepository;

    @Override
    @Transactional
    public void approveDoctor(Long doctorUserId) {
        Doctor doctor = doctorRepository.findById(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        doctor.setIsApproved(true);
        doctor.getUser().setIsActive(true);
        userRepository.save(doctor.getUser());
        doctorRepository.save(doctor);
        redisCacheService.evictDoctorCache(doctor.getSpecialty() != null ? doctor.getSpecialty().getId() : null);
    }

    @Override
    @Transactional
    public void rejectDoctor(Long doctorUserId) {
        Doctor doctor = doctorRepository.findById(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        doctor.setIsApproved(false);
        doctor.getUser().setIsActive(false);
        userRepository.save(doctor.getUser());
        doctorRepository.save(doctor);
        redisCacheService.evictDoctorCache(doctor.getSpecialty() != null ? doctor.getSpecialty().getId() : null);
    }

    @Override
    @Transactional
    public SpecialtyDto createSpecialty(SpecialtyRequestDto request) {
        Specialty specialty = Specialty.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Specialty saved = specialtyRepository.save(specialty);
        return SpecialtyDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyDto> getAllSpecialties() {
        return specialtyRepository.findAll().stream()
                .map(s -> SpecialtyDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> getPendingDoctors() {
        return doctorRepository.findAll().stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsApproved()) && Boolean.TRUE.equals(d.getUser().getIsActive()))
                .map(this::mapDoctor)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalDoctors = doctorRepository.count();
        long totalPatients = patientRepository.count();
        long totalAppointments = appointmentRepository.count();
        stats.put("totalDoctors", totalDoctors);
        stats.put("totalPatients", totalPatients);
        stats.put("totalAppointments", totalAppointments);
        stats.put("totalSpecialties", specialtyRepository.count());
        long cancelled = appointmentRepository.countByStatus("CANCELLED");
        stats.put("cancelledAppointments", cancelled);
        stats.put("cancellationRate", totalAppointments == 0 ? 0.0 : Math.round(cancelled * 10000.0 / totalAppointments) / 100.0);
        java.math.BigDecimal revenue = appointmentRepository.findAll().stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()) && a.getPrice() != null)
                .map(com.datlichkhambenh.backend.entity.Appointment::getPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        stats.put("totalRevenue", revenue);
        stats.put("topDoctors", appointmentRepository.findTopDoctors().stream().limit(5)
                .map(row -> Map.of("doctorName", row[0], "appointments", row[1])).toList());
        return stats;
    }

    @Override
    public List<UserDto> getUsers(String role) {
        return userRepository.findAll().stream().filter(u -> role == null || role.equalsIgnoreCase(u.getRole()))
                .map(u -> UserDto.builder().id(u.getId()).email(u.getEmail()).role(u.getRole())
                        .isActive(u.getIsActive()).createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt()).build()).toList();
    }

    @Override @Transactional
    public void setUserActive(Long userId, boolean active) {
        com.datlichkhambenh.backend.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!active && "ADMIN".equals(user.getRole())
                && userRepository.countByRoleAndIsActiveTrue("ADMIN") <= 1) {
            throw new com.datlichkhambenh.backend.exception.AppException("Không thể khóa quản trị viên hoạt động cuối cùng");
        }
        user.setIsActive(active); userRepository.save(user);
        if ("DOCTOR".equals(user.getRole())) {
            doctorRepository.findById(userId).ifPresent(d ->
                    redisCacheService.evictDoctorCache(d.getSpecialty().getId()));
        }
    }

    @Override @Transactional
    public SpecialtyDto updateSpecialty(Long id, SpecialtyRequestDto request) {
        Specialty s = specialtyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
        s.setName(request.getName()); s.setDescription(request.getDescription()); specialtyRepository.save(s);
        redisCacheService.evictDoctorCache(id);
        return SpecialtyDto.builder().id(s.getId()).name(s.getName()).description(s.getDescription()).build();
    }

    @Override @Transactional
    public void deleteSpecialty(Long id) {
        Specialty s = specialtyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
        if (!doctorRepository.findBySpecialtyId(id).isEmpty() || medicalServiceRepository.existsBySpecialtyId(id)) {
            throw new com.datlichkhambenh.backend.exception.AppException("Không thể xóa chuyên khoa đang được sử dụng");
        }
        specialtyRepository.delete(s); redisCacheService.evictDoctorCache(id);
    }

    @Override @Transactional(readOnly = true)
    public Map<String, Object> getAnalytics(int year) {
        List<com.datlichkhambenh.backend.entity.Appointment> yearly = appointmentRepository.findAll().stream()
                .filter(a -> a.getSchedule() != null && a.getSchedule().getWorkDate().getYear() == year).toList();

        List<Map<String, Object>> months = new java.util.ArrayList<>();
        List<Map<String, Object>> allTransactions = new java.util.ArrayList<>();
        java.math.BigDecimal totalYearRevenue = java.math.BigDecimal.ZERO;
        long totalYearApps = 0;
        java.math.BigDecimal prevMonthRevenue = java.math.BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            final int m = month;
            List<com.datlichkhambenh.backend.entity.Appointment> entries = yearly.stream()
                    .filter(a -> a.getSchedule().getWorkDate().getMonthValue() == m).toList();

            long completed = entries.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
            long cancelled = entries.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
            long totalMonthApps = entries.size();

            java.math.BigDecimal monthTotalRevenue = entries.stream()
                    .filter(a -> "COMPLETED".equals(a.getStatus()) && a.getPrice() != null)
                    .map(com.datlichkhambenh.backend.entity.Appointment::getPrice)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            double growthPercent = 0.0;
            if (m > 1 && prevMonthRevenue.compareTo(java.math.BigDecimal.ZERO) > 0) {
                growthPercent = monthTotalRevenue.subtract(prevMonthRevenue)
                        .divide(prevMonthRevenue, 4, java.math.RoundingMode.HALF_UP)
                        .doubleValue() * 100;
            }
            prevMonthRevenue = monthTotalRevenue;

            totalYearRevenue = totalYearRevenue.add(monthTotalRevenue);
            totalYearApps += totalMonthApps;

            for (com.datlichkhambenh.backend.entity.Appointment app : entries) {
                Map<String, Object> tx = new HashMap<>();
                tx.put("transactionId", String.format("TXN-%d%02d-%05d", year, m, app.getId()));
                tx.put("bookingCode", app.getBookingCode());
                tx.put("patientName", app.getPatient() != null ? app.getPatient().getFullName() : "Bệnh nhân");
                tx.put("patientPhone", app.getPatient() != null ? app.getPatient().getPhone() : "");
                tx.put("amount", app.getPrice() != null ? app.getPrice() : java.math.BigDecimal.ZERO);
                tx.put("paymentMethod", "VietQR Banking");
                tx.put("paymentDate", app.getCreatedAt() != null ? app.getCreatedAt().toString() : "");
                tx.put("month", m);
                tx.put("doctorName", app.getDoctor() != null ? app.getDoctor().getFullName() : "Bác sĩ");
                tx.put("specialty", app.getDoctor() != null && app.getDoctor().getSpecialty() != null ? app.getDoctor().getSpecialty().getName() : "Đa khoa");
                tx.put("status", "COMPLETED".equals(app.getStatus()) ? "SUCCESS" : app.getStatus());
                allTransactions.add(tx);
            }

            months.add(Map.of(
                "month", month,
                "appointments", totalMonthApps,
                "completed", completed,
                "cancelled", cancelled,
                "revenue", monthTotalRevenue.doubleValue(),
                "growthPercent", Math.round(growthPercent * 10.0) / 10.0
            ));
        }

        Map<Long, List<com.datlichkhambenh.backend.entity.Appointment>> byDoctor = yearly.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getDoctor().getUserId()));
        List<Map<String, Object>> rankings = byDoctor.values().stream().map(list -> {
            com.datlichkhambenh.backend.entity.Doctor doctor = list.get(0).getDoctor();
            java.math.BigDecimal revenue = list.stream().filter(a -> "COMPLETED".equals(a.getStatus()) && a.getPrice() != null)
                    .map(com.datlichkhambenh.backend.entity.Appointment::getPrice)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            return Map.<String, Object>of(
                    "doctorId", doctor.getUserId(),
                    "doctorName", doctor.getFullName(),
                    "specialty", doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : "Đa khoa",
                    "appointments", list.size(),
                    "confirmed", list.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count(),
                    "completed", list.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count(),
                    "cancelled", list.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count(),
                    "revenue", revenue.doubleValue()
            );
        }).sorted((a,b) -> Double.compare((Double)b.get("revenue"), (Double)a.get("revenue"))).toList();

        Map<String, Long> statusCounts = Map.of(
                "CONFIRMED", yearly.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count(),
                "COMPLETED", yearly.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count(),
                "CANCELLED", yearly.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count());

        List<Map<String, Object>> dailyRevenue = yearly.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()) && a.getPrice() != null)
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getSchedule().getWorkDate(),
                        java.util.TreeMap::new,
                        java.util.stream.Collectors.reducing(java.math.BigDecimal.ZERO,
                                com.datlichkhambenh.backend.entity.Appointment::getPrice, java.math.BigDecimal::add)))
                .entrySet().stream().map(e -> Map.<String, Object>of("date", e.getKey(), "revenue", e.getValue())).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("months", months);
        result.put("doctorRankings", rankings);
        result.put("statusCounts", statusCounts);
        result.put("dailyRevenue", dailyRevenue);
        // Trả về toàn bộ giao dịch (hiện chỉ 60 bản/năm — 5 tx × 12 tháng)
        result.put("transactions", allTransactions);
        result.put("totalTransactions", allTransactions.size());
        result.put("yearAppointments", totalYearApps);
        result.put("yearRevenue", totalYearRevenue.doubleValue());
        result.put("avgPerAppointment", totalYearApps > 0
                ? totalYearRevenue.divide(new java.math.BigDecimal(totalYearApps), 0, java.math.RoundingMode.HALF_UP).doubleValue()
                : 0.0);

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> getDoctors() {
        return doctorRepository.findAll().stream().map(this::mapDoctor).toList();
    }

    @Override
    @Transactional
    public DoctorDto createDoctor(AdminCreateDoctorDto request) {
        validateUniqueIdentity(request.getEmail(), request.getPhone());
        User user = userRepository.save(User.builder()
                .email(normalizeEmail(request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("DOCTOR").isActive(true).build());
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
        Doctor doctor = doctorRepository.save(Doctor.builder().user(user).specialty(specialty)
                .fullName(request.getFullName().trim()).phone(request.getPhone()).degree(request.getDegree())
                .experienceYears(request.getExperienceYears()).consultationFee(request.getConsultationFee())
                .isApproved(request.getApproved() == null || request.getApproved()).build());
        redisCacheService.evictDoctorCache(specialty.getId());
        return mapDoctor(doctor);
    }

    @Override
    public DoctorDto updateDoctor(Long doctorId, UpdateDoctorProfileDto request) {
        return doctorService.updateProfile(doctorId, request);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        if (appointmentRepository.existsByDoctorUserId(doctorId) || scheduleRepository.existsByDoctorUserId(doctorId)) {
            throw new com.datlichkhambenh.backend.exception.AppException(
                    "Không thể xóa bác sĩ đã có lịch làm việc hoặc lịch hẹn; hãy khóa tài khoản thay thế");
        }
        Long specialtyId = doctor.getSpecialty().getId();
        User user = doctor.getUser();
        doctorRepository.delete(doctor);
        doctorRepository.flush();
        userRepository.delete(user);
        redisCacheService.evictDoctorCache(specialtyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> getPatients() {
        return patientRepository.findAll().stream().map(this::mapPatient).toList();
    }

    @Override
    @Transactional
    public PatientDto createPatient(AdminCreatePatientDto request) {
        validateUniqueIdentity(request.getEmail(), request.getPhone());
        User user = userRepository.save(User.builder()
                .email(normalizeEmail(request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("PATIENT").isActive(true).build());
        Patient patient = patientRepository.save(Patient.builder().user(user).fullName(request.getFullName().trim())
                .phone(request.getPhone()).dob(request.getDob()).gender(request.getGender())
                .address(request.getAddress()).medicalHistory(request.getMedicalHistory()).build());
        return mapPatient(patient);
    }

    @Override
    public PatientDto updatePatient(Long patientId, UpdatePatientProfileDto request) {
        return patientService.updateProfile(patientId, request);
    }

    @Override
    @Transactional
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));
        if (appointmentRepository.existsByPatientUserId(patientId)) {
            throw new com.datlichkhambenh.backend.exception.AppException(
                    "Không thể xóa bệnh nhân đã có lịch sử khám; hãy khóa tài khoản thay thế");
        }
        User user = patient.getUser();
        patientRepository.delete(patient);
        patientRepository.flush();
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointments(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                safePage, safeSize, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return appointmentRepository.findAll(pageable).stream().map(this::mapAppointment).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientUserId(patientId).stream().map(this::mapAppointment).toList();
    }

    private AppointmentDto mapAppointment(com.datlichkhambenh.backend.entity.Appointment a) {
        com.datlichkhambenh.backend.entity.Schedule s = a.getSchedule();
        return AppointmentDto.builder()
                .id(a.getId())
                .bookingCode(a.getBookingCode())
                .patientId(a.getPatient() != null ? a.getPatient().getUserId() : null)
                .patientName(a.getPatient() != null ? a.getPatient().getFullName() : null)
                .patientPhone(a.getPatient() != null ? a.getPatient().getPhone() : null)
                .doctorId(a.getDoctor() != null ? a.getDoctor().getUserId() : null)
                .doctorName(a.getDoctor() != null ? a.getDoctor().getFullName() : null)
                .scheduleId(s != null ? s.getId() : null)
                .workDate(s != null ? s.getWorkDate() : null)
                .startTime(s != null ? s.getStartTime() : null)
                .endTime(s != null ? s.getEndTime() : null)
                .symptoms(a.getSymptoms())
                .status(a.getStatus())
                .price(a.getPrice())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private void validateUniqueIdentity(String email, String phone) {
        if (userRepository.existsByEmailIgnoreCase(normalizeEmail(email))) {
            throw new com.datlichkhambenh.backend.exception.AppException("Email đã tồn tại");
        }
        if (phone != null && (doctorRepository.existsByPhone(phone) || patientRepository.existsByPhone(phone))) {
            throw new com.datlichkhambenh.backend.exception.AppException("Số điện thoại đã tồn tại");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private DoctorDto mapDoctor(Doctor d) {
        return DoctorDto.builder().userId(d.getUserId()).email(d.getUser().getEmail())
                .fullName(d.getFullName()).phone(d.getPhone()).degree(d.getDegree())
                .experienceYears(d.getExperienceYears()).consultationFee(d.getConsultationFee())
                .isApproved(d.getIsApproved()).specialtyId(d.getSpecialty().getId())
                .specialtyName(d.getSpecialty().getName()).build();
    }

    private PatientDto mapPatient(Patient p) {
        return PatientDto.builder().userId(p.getUserId()).email(p.getUser().getEmail())
                .fullName(p.getFullName()).phone(p.getPhone()).dob(p.getDob()).gender(p.getGender())
                .address(p.getAddress()).medicalHistory(p.getMedicalHistory()).build();
    }
}
