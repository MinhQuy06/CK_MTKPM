package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.dto.request.AdminCreateDoctorDto;
import com.datlichkhambenh.backend.dto.request.AdminCreatePatientDto;
import com.datlichkhambenh.backend.dto.request.SpecialtyRequestDto;
import com.datlichkhambenh.backend.dto.request.UpdateDoctorProfileDto;
import com.datlichkhambenh.backend.dto.request.UpdatePatientProfileDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.dto.response.DoctorDto;
import com.datlichkhambenh.backend.dto.response.PatientDto;
import com.datlichkhambenh.backend.dto.response.SpecialtyDto;
import com.datlichkhambenh.backend.dto.response.UserDto;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.entity.Specialty;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.factory.AccountFactories;
import com.datlichkhambenh.backend.factory.AccountRequest;
import com.datlichkhambenh.backend.factory.ClinicAccount;
import com.datlichkhambenh.backend.repository.*;
import com.datlichkhambenh.backend.service.AdminManagementService;
import com.datlichkhambenh.backend.service.DoctorService;
import com.datlichkhambenh.backend.service.PatientService;
import com.datlichkhambenh.backend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminManagementServiceImpl implements AdminManagementService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentDtoMapper appointmentMapper;
    private final RedisCacheService redisCacheService;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final MedicalServiceRepository medicalServiceRepository;
    private final AccountFactories accounts;

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
        BigDecimal revenue = appointmentRepository.findAll().stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()) && a.getPrice() != null)
                .map(Appointment::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!active && "ADMIN".equals(user.getRole())
                && userRepository.countByRoleAndIsActiveTrue("ADMIN") <= 1) {
            throw new AppException("Không thể khóa quản trị viên hoạt động cuối cùng");
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
            throw new AppException("Không thể xóa chuyên khoa đang được sử dụng");
        }
        specialtyRepository.delete(s); redisCacheService.evictDoctorCache(id);
    }

    @Override @Transactional(readOnly = true)
    public Map<String, Object> getAnalytics(int year) {
        List<Appointment> yearly = appointmentRepository.findAll().stream()
                .filter(a -> a.getSchedule() != null && a.getSchedule().getWorkDate().getYear() == year).toList();

        List<Map<String, Object>> months = new ArrayList<>();
        List<Map<String, Object>> allTransactions = new ArrayList<>();
        BigDecimal totalYearRevenue = BigDecimal.ZERO;
        long totalYearApps = 0;
        BigDecimal prevMonthRevenue = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            final int m = month;
            List<Appointment> entries = yearly.stream()
                    .filter(a -> a.getSchedule().getWorkDate().getMonthValue() == m).toList();

            long completed = entries.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
            long cancelled = entries.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
            long totalMonthApps = entries.size();

            BigDecimal monthTotalRevenue = entries.stream()
                    .filter(a -> "COMPLETED".equals(a.getStatus()) && a.getPrice() != null)
                    .map(Appointment::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double growthPercent = 0.0;
            if (m > 1 && prevMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
                growthPercent = monthTotalRevenue.subtract(prevMonthRevenue)
                        .divide(prevMonthRevenue, 4, RoundingMode.HALF_UP)
                        .doubleValue() * 100;
            }
            prevMonthRevenue = monthTotalRevenue;

            totalYearRevenue = totalYearRevenue.add(monthTotalRevenue);
            totalYearApps += totalMonthApps;

            // Hệ thống chưa có chức năng thanh toán: đây là danh sách lịch hẹn kèm phí khám đã chốt,
            // với trạng thái thật của lịch hẹn (không phải giao dịch thanh toán).
            for (Appointment app : entries) {
                Map<String, Object> row = new HashMap<>();
                row.put("appointmentId", app.getId());
                row.put("bookingCode", app.getBookingCode());
                row.put("patientName", app.getPatient() != null ? app.getPatient().getFullName() : null);
                row.put("patientPhone", app.getPatient() != null ? app.getPatient().getPhone() : null);
                row.put("doctorName", app.getDoctor() != null ? app.getDoctor().getFullName() : null);
                row.put("specialty", app.getDoctor() != null && app.getDoctor().getSpecialty() != null ? app.getDoctor().getSpecialty().getName() : null);
                row.put("workDate", app.getSchedule().getWorkDate().toString());
                row.put("startTime", app.getSchedule().getStartTime() != null ? app.getSchedule().getStartTime().toString() : null);
                row.put("amount", app.getPrice() != null ? app.getPrice() : BigDecimal.ZERO);
                row.put("pricingKey", app.getPricingKey());
                row.put("status", app.getStatus());
                row.put("month", m);
                allTransactions.add(row);
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

        Map<Long, List<Appointment>> byDoctor = yearly.stream()
                .collect(Collectors.groupingBy(a -> a.getDoctor().getUserId()));
        List<Map<String, Object>> rankings = byDoctor.values().stream().map(list -> {
            Doctor doctor = list.get(0).getDoctor();
            BigDecimal revenue = list.stream().filter(a -> "COMPLETED".equals(a.getStatus()) && a.getPrice() != null)
                    .map(Appointment::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
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
                .collect(Collectors.groupingBy(a -> a.getSchedule().getWorkDate(),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO,
                                Appointment::getPrice, BigDecimal::add)))
                .entrySet().stream().map(e -> Map.<String, Object>of("date", e.getKey(), "revenue", e.getValue())).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("months", months);
        result.put("doctorRankings", rankings);
        result.put("statusCounts", statusCounts);
        result.put("dailyRevenue", dailyRevenue);
        // Lịch hẹn kèm phí khám trong năm (doanh thu chỉ tính lịch đã khám - COMPLETED)
        allTransactions.sort((a, b) -> ((String) b.get("workDate")).compareTo((String) a.get("workDate")));
        result.put("appointmentFees", allTransactions);
        result.put("totalAppointmentFees", allTransactions.size());
        result.put("yearAppointments", totalYearApps);
        result.put("yearRevenue", totalYearRevenue.doubleValue());
        result.put("avgPerAppointment", totalYearApps > 0
                ? totalYearRevenue.divide(new BigDecimal(totalYearApps), 0, RoundingMode.HALF_UP).doubleValue()
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
        // Bác sĩ do Admin tạo được duyệt sẵn, trừ khi Admin chọn để chờ duyệt
        var account = (ClinicAccount.DoctorAccount) accounts.forRole("DOCTOR").orElseThrow().create(new AccountRequest(
                normalizeEmail(request.getEmail()), request.getPassword(), request.getFullName().trim(),
                request.getPhone(), null, null, null, null, request.getSpecialtyId(), request.getDegree(),
                request.getExperienceYears(), request.getConsultationFee(),
                request.getApproved() == null || request.getApproved()));
        Doctor doctor = account.doctor();
        redisCacheService.evictDoctorCache(doctor.getSpecialty().getId());
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
            throw new AppException(
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
        var account = (ClinicAccount.PatientAccount) accounts.forRole("PATIENT").orElseThrow().create(new AccountRequest(
                normalizeEmail(request.getEmail()), request.getPassword(), request.getFullName().trim(),
                request.getPhone(), request.getDob(), request.getGender(), request.getAddress(),
                request.getMedicalHistory(), null, null, null, null, false));
        return mapPatient(account.patient());
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
            throw new AppException(
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
        Pageable pageable = PageRequest.of(
                safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return appointmentMapper.toDtos(appointmentRepository.findAll(pageable).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getPatientAppointments(Long patientId) {
        return appointmentMapper.toDtos(appointmentRepository.findByPatientUserId(patientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorAppointments(Long doctorUserId) {
        if (!doctorRepository.existsById(doctorUserId)) {
            throw new ResourceNotFoundException("Bác sĩ không tồn tại");
        }
        List<AppointmentDto> list = appointmentMapper.toDtos(appointmentRepository.findByDoctorUserId(doctorUserId));
        list.sort((a, b) -> {
            int byDate = b.getWorkDate().compareTo(a.getWorkDate());
            return byDate != 0 ? byDate : a.getStartTime().compareTo(b.getStartTime());
        });
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDoctorAppointmentStats() {
        return appointmentRepository.countByDoctorAndStatus().stream()
                .collect(Collectors.groupingBy(row -> (Long) row[0], LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream().map(e -> {
                    Map<String, Object> stat = new LinkedHashMap<>();
                    stat.put("doctorId", e.getKey());
                    long total = 0;
                    for (Object[] row : e.getValue()) {
                        long count = (Long) row[2];
                        stat.put(((String) row[1]).toLowerCase(), count);
                        total += count;
                    }
                    stat.put("total", total);
                    return stat;
                }).toList();
    }

    private void validateUniqueIdentity(String email, String phone) {
        if (userRepository.existsByEmailIgnoreCase(normalizeEmail(email))) {
            throw new AppException("Email đã tồn tại");
        }
        if (phone != null && (doctorRepository.existsByPhone(phone) || patientRepository.existsByPhone(phone))) {
            throw new AppException("Số điện thoại đã tồn tại");
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
