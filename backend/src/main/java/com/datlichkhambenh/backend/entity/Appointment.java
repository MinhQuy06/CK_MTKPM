package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "appointments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_appointments_booking_code", columnNames = "booking_code"),
        @UniqueConstraint(name = "uk_appointments_schedule",     columnNames = "schedule_id")
    }
)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 100)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "patient_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_appointments_patient")
    )
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "doctor_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_appointments_doctor")
    )
    private Doctor doctor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "schedule_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_appointments_schedule")
    )
    private Schedule schedule;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Appointment() {}

    public Appointment(Long id, String bookingCode, Patient patient, Doctor doctor, Schedule schedule, String symptoms, String status, BigDecimal price, LocalDateTime createdAt) {
        this.id = id;
        this.bookingCode = bookingCode;
        this.patient = patient;
        this.doctor = doctor;
        this.schedule = schedule;
        this.symptoms = symptoms;
        this.status = status;
        this.price = price;
        this.createdAt = createdAt;
    }

    public static AppointmentBuilder builder() {
        return new AppointmentBuilder();
    }

    public static class AppointmentBuilder {
        private Long id;
        private String bookingCode;
        private Patient patient;
        private Doctor doctor;
        private Schedule schedule;
        private String symptoms;
        private String status;
        private BigDecimal price;
        private LocalDateTime createdAt;

        public AppointmentBuilder id(Long id) { this.id = id; return this; }
        public AppointmentBuilder bookingCode(String bookingCode) { this.bookingCode = bookingCode; return this; }
        public AppointmentBuilder patient(Patient patient) { this.patient = patient; return this; }
        public AppointmentBuilder doctor(Doctor doctor) { this.doctor = doctor; return this; }
        public AppointmentBuilder schedule(Schedule schedule) { this.schedule = schedule; return this; }
        public AppointmentBuilder symptoms(String symptoms) { this.symptoms = symptoms; return this; }
        public AppointmentBuilder status(String status) { this.status = status; return this; }
        public AppointmentBuilder price(BigDecimal price) { this.price = price; return this; }
        public AppointmentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Appointment build() {
            return new Appointment(id, bookingCode, patient, doctor, schedule, symptoms, status, price, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
