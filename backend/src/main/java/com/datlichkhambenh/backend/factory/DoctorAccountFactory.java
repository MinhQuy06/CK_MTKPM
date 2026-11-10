package com.datlichkhambenh.backend.factory;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Specialty;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import com.datlichkhambenh.backend.repository.SpecialtyRepository;
import com.datlichkhambenh.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Concrete Creator: doctor accounts, attached to a specialty, approved or waiting for Admin. */
@Component
public class DoctorAccountFactory extends AccountFactory {
    private final DoctorRepository doctors;
    private final SpecialtyRepository specialties;

    public DoctorAccountFactory(UserRepository users, PasswordEncoder passwordEncoder,
                                DoctorRepository doctors, SpecialtyRepository specialties) {
        super(users, passwordEncoder);
        this.doctors = doctors;
        this.specialties = specialties;
    }

    @Override
    public String role() {
        return "DOCTOR";
    }

    @Override
    protected ClinicAccount createProfile(User user, AccountRequest request) {
        if (request.specialtyId() == null) {
            throw new AppException("Bác sĩ phải đăng ký thuộc 1 chuyên khoa (specialtyId)");
        }
        Specialty specialty = specialties.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
        Doctor doctor = doctors.save(Doctor.builder()
                .user(user)
                .specialty(specialty)
                .fullName(request.fullName())
                .phone(request.phone())
                .degree(request.degree())
                .experienceYears(request.experienceYears())
                .consultationFee(request.consultationFee())
                .isApproved(request.approved())
                .build());
        return new ClinicAccount.DoctorAccount(user, doctor);
    }
}
