package com.datlichkhambenh.backend.factory;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.User;

/** Product (GoF Factory Method): a login together with the role-specific profile created for it. */
public sealed interface ClinicAccount permits ClinicAccount.PatientAccount, ClinicAccount.DoctorAccount {

    User user();

    /** Concrete Product: login + patient profile. */
    record PatientAccount(User user, Patient patient) implements ClinicAccount {
    }

    /** Concrete Product: login + doctor profile. */
    record DoctorAccount(User user, Doctor doctor) implements ClinicAccount {
    }
}
