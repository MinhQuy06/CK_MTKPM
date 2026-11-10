package com.datlichkhambenh.backend.factory;

import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Concrete Creator: patient accounts. */
@Component
public class PatientAccountFactory extends AccountFactory {
    private final PatientRepository patients;

    public PatientAccountFactory(UserRepository users, PasswordEncoder passwordEncoder, PatientRepository patients) {
        super(users, passwordEncoder);
        this.patients = patients;
    }

    @Override
    public String role() {
        return "PATIENT";
    }

    @Override
    protected ClinicAccount createProfile(User user, AccountRequest request) {
        Patient patient = patients.save(Patient.builder()
                .user(user)
                .fullName(request.fullName())
                .phone(request.phone())
                .dob(request.dob())
                .gender(request.gender())
                .address(request.address())
                .medicalHistory(request.medicalHistory())
                .build());
        return new ClinicAccount.PatientAccount(user, patient);
    }
}
