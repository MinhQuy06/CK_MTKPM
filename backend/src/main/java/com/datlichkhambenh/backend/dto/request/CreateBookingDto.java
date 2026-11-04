package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class CreateBookingDto {

    @NotNull(message = "scheduleId không được để trống")
    private Long scheduleId;

    private String symptoms;

    public CreateBookingDto() {}

    public CreateBookingDto(Long scheduleId, String symptoms) {
        this.scheduleId = scheduleId;
        this.symptoms = symptoms;
    }

    public static CreateBookingDtoBuilder builder() {
        return new CreateBookingDtoBuilder();
    }

    public static class CreateBookingDtoBuilder {
        private Long scheduleId;
        private String symptoms;

        public CreateBookingDtoBuilder scheduleId(Long scheduleId) { this.scheduleId = scheduleId; return this; }
        public CreateBookingDtoBuilder symptoms(String symptoms) { this.symptoms = symptoms; return this; }

        public CreateBookingDto build() {
            return new CreateBookingDto(scheduleId, symptoms);
        }
    }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
}
