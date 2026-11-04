package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "patients",
    uniqueConstraints = @UniqueConstraint(name = "uk_patients_phone", columnNames = "phone")
)
public class Patient {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_patients_user"))
    private User user;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    public Patient() {}

    public Patient(Long userId, User user, String fullName, LocalDate dob, String gender, String phone, String address, String medicalHistory) {
        this.userId = userId;
        this.user = user;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.medicalHistory = medicalHistory;
    }

    public static PatientBuilder builder() {
        return new PatientBuilder();
    }

    public static class PatientBuilder {
        private Long userId;
        private User user;
        private String fullName;
        private LocalDate dob;
        private String gender;
        private String phone;
        private String address;
        private String medicalHistory;

        public PatientBuilder userId(Long userId) { this.userId = userId; return this; }
        public PatientBuilder user(User user) { this.user = user; return this; }
        public PatientBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public PatientBuilder dob(LocalDate dob) { this.dob = dob; return this; }
        public PatientBuilder gender(String gender) { this.gender = gender; return this; }
        public PatientBuilder phone(String phone) { this.phone = phone; return this; }
        public PatientBuilder address(String address) { this.address = address; return this; }
        public PatientBuilder medicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; return this; }

        public Patient build() {
            return new Patient(userId, user, fullName, dob, gender, phone, address, medicalHistory);
        }
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
}
