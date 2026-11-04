package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "medical_records",
    uniqueConstraints = @UniqueConstraint(name = "uk_medical_records_appointment", columnNames = "appointment_id")
)
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "appointment_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_medical_records_appointment")
    )
    private Appointment appointment;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MedicalRecord() {}

    public MedicalRecord(Long id, Appointment appointment, String diagnosis, String doctorNotes, LocalDateTime createdAt) {
        this.id = id;
        this.appointment = appointment;
        this.diagnosis = diagnosis;
        this.doctorNotes = doctorNotes;
        this.createdAt = createdAt;
    }

    public static MedicalRecordBuilder builder() {
        return new MedicalRecordBuilder();
    }

    public static class MedicalRecordBuilder {
        private Long id;
        private Appointment appointment;
        private String diagnosis;
        private String doctorNotes;
        private LocalDateTime createdAt;

        public MedicalRecordBuilder id(Long id) { this.id = id; return this; }
        public MedicalRecordBuilder appointment(Appointment appointment) { this.appointment = appointment; return this; }
        public MedicalRecordBuilder diagnosis(String diagnosis) { this.diagnosis = diagnosis; return this; }
        public MedicalRecordBuilder doctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; return this; }
        public MedicalRecordBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public MedicalRecord build() {
            return new MedicalRecord(id, appointment, diagnosis, doctorNotes, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
