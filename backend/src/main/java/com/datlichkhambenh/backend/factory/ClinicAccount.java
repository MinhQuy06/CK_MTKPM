package com.datlichkhambenh.backend.factory;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.User;

/** Product (GoF Factory Method): một login cùng hồ sơ riêng của vai trò được tạo cho nó. */
public sealed interface ClinicAccount permits ClinicAccount.PatientAccount, ClinicAccount.DoctorAccount {

    User user();

    /** Concrete Product: login + hồ sơ bệnh nhân. */
    record PatientAccount(User user, Patient patient) implements ClinicAccount {
    }

    /** Concrete Product: login + hồ sơ bác sĩ. */
    record DoctorAccount(User user, Doctor doctor) implements ClinicAccount {
    }
}
