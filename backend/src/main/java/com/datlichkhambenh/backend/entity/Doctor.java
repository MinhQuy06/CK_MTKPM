package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "doctors",
    uniqueConstraints = @UniqueConstraint(name = "uk_doctors_phone", columnNames = "phone")
)
public class Doctor {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_doctors_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "specialty_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_doctors_specialty")
    )
    private Specialty specialty;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "degree", length = 100)
    private String degree;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "consultation_fee", precision = 12, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

    public Doctor() {}

    public Doctor(Long userId, User user, Specialty specialty, String fullName, String phone, String degree, Integer experienceYears, BigDecimal consultationFee, Boolean isApproved) {
        this.userId = userId;
        this.user = user;
        this.specialty = specialty;
        this.fullName = fullName;
        this.phone = phone;
        this.degree = degree;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.isApproved = isApproved != null ? isApproved : false;
    }

    public static DoctorBuilder builder() {
        return new DoctorBuilder();
    }

    public static class DoctorBuilder {
        private Long userId;
        private User user;
        private Specialty specialty;
        private String fullName;
        private String phone;
        private String degree;
        private Integer experienceYears;
        private BigDecimal consultationFee;
        private Boolean isApproved = false;

        public DoctorBuilder userId(Long userId) { this.userId = userId; return this; }
        public DoctorBuilder user(User user) { this.user = user; return this; }
        public DoctorBuilder specialty(Specialty specialty) { this.specialty = specialty; return this; }
        public DoctorBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public DoctorBuilder phone(String phone) { this.phone = phone; return this; }
        public DoctorBuilder degree(String degree) { this.degree = degree; return this; }
        public DoctorBuilder experienceYears(Integer experienceYears) { this.experienceYears = experienceYears; return this; }
        public DoctorBuilder consultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; return this; }
        public DoctorBuilder isApproved(Boolean isApproved) { this.isApproved = isApproved; return this; }

        public Doctor build() {
            return new Doctor(userId, user, specialty, fullName, phone, degree, experienceYears, consultationFee, isApproved);
        }
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty specialty) { this.specialty = specialty; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
}
