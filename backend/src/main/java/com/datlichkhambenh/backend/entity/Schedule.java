package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "doctor_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_schedules_doctor")
    )
    private Doctor doctor;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    public Schedule() {}

    public Schedule(Long id, Doctor doctor, LocalDate workDate, LocalTime startTime, LocalTime endTime, Boolean isAvailable) {
        this.id = id;
        this.doctor = doctor;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable != null ? isAvailable : true;
    }

    public static ScheduleBuilder builder() {
        return new ScheduleBuilder();
    }

    public static class ScheduleBuilder {
        private Long id;
        private Doctor doctor;
        private LocalDate workDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Boolean isAvailable = true;

        public ScheduleBuilder id(Long id) { this.id = id; return this; }
        public ScheduleBuilder doctor(Doctor doctor) { this.doctor = doctor; return this; }
        public ScheduleBuilder workDate(LocalDate workDate) { this.workDate = workDate; return this; }
        public ScheduleBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public ScheduleBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public ScheduleBuilder isAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; return this; }

        public Schedule build() {
            return new Schedule(id, doctor, workDate, startTime, endTime, isAvailable);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
