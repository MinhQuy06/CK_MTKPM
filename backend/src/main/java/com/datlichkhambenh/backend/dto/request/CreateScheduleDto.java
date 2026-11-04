package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateScheduleDto {

    @NotNull(message = "workDate không được để trống")
    private LocalDate workDate;

    @NotNull(message = "startTime không được để trống")
    private LocalTime startTime;

    @NotNull(message = "endTime không được để trống")
    private LocalTime endTime;

    public CreateScheduleDto() {}

    public CreateScheduleDto(LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static CreateScheduleDtoBuilder builder() {
        return new CreateScheduleDtoBuilder();
    }

    public static class CreateScheduleDtoBuilder {
        private LocalDate workDate;
        private LocalTime startTime;
        private LocalTime endTime;

        public CreateScheduleDtoBuilder workDate(LocalDate workDate) { this.workDate = workDate; return this; }
        public CreateScheduleDtoBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public CreateScheduleDtoBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }

        public CreateScheduleDto build() {
            return new CreateScheduleDto(workDate, startTime, endTime);
        }
    }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
