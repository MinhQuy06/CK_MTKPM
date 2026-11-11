package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateBookingDto {

    @NotNull(message = "scheduleId không được để trống")
    private Long scheduleId;

    private String symptoms;
    private LocalDate expectedWorkDate;
    private LocalTime expectedStartTime, expectedEndTime;
    private String strategyKey;
    private BigDecimal expectedBasePrice;
    private BigDecimal expectedDiscountRate;
    private BigDecimal expectedPrice;

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
        private String strategyKey;
        private LocalDate expectedWorkDate;
        private LocalTime expectedStartTime, expectedEndTime;
        private BigDecimal expectedBasePrice, expectedDiscountRate, expectedPrice;

        public CreateBookingDtoBuilder scheduleId(Long scheduleId) { this.scheduleId = scheduleId; return this; }
        public CreateBookingDtoBuilder symptoms(String symptoms) { this.symptoms = symptoms; return this; }

        public CreateBookingDtoBuilder expectedWorkDate(LocalDate value){this.expectedWorkDate=value;return this;}
        public CreateBookingDtoBuilder expectedStartTime(LocalTime value){this.expectedStartTime=value;return this;}
        public CreateBookingDtoBuilder expectedEndTime(LocalTime value){this.expectedEndTime=value;return this;}
        public CreateBookingDtoBuilder strategyKey(String value){this.strategyKey=value;return this;}
        public CreateBookingDtoBuilder expectedBasePrice(BigDecimal value){this.expectedBasePrice=value;return this;}
        public CreateBookingDtoBuilder expectedDiscountRate(BigDecimal value){this.expectedDiscountRate=value;return this;}
        public CreateBookingDtoBuilder expectedPrice(BigDecimal value){this.expectedPrice=value;return this;}
        public CreateBookingDto build() {
            var dto=new CreateBookingDto(scheduleId,symptoms);dto.strategyKey=strategyKey;dto.expectedBasePrice=expectedBasePrice;
            dto.expectedWorkDate=expectedWorkDate;dto.expectedStartTime=expectedStartTime;dto.expectedEndTime=expectedEndTime;
            dto.expectedDiscountRate=expectedDiscountRate;dto.expectedPrice=expectedPrice;return dto;
        }
    }

    public LocalDate getExpectedWorkDate(){return expectedWorkDate;}
    public void setExpectedWorkDate(LocalDate value){expectedWorkDate=value;}
    public LocalTime getExpectedStartTime(){return expectedStartTime;}
    public void setExpectedStartTime(LocalTime value){expectedStartTime=value;}
    public LocalTime getExpectedEndTime(){return expectedEndTime;}
    public void setExpectedEndTime(LocalTime value){expectedEndTime=value;}

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getStrategyKey(){return strategyKey;}
    public void setStrategyKey(String value){strategyKey=value;}
    public BigDecimal getExpectedBasePrice(){return expectedBasePrice;}
    public void setExpectedBasePrice(BigDecimal value){expectedBasePrice=value;}
    public BigDecimal getExpectedDiscountRate(){return expectedDiscountRate;}
    public void setExpectedDiscountRate(BigDecimal value){expectedDiscountRate=value;}
    public BigDecimal getExpectedPrice(){return expectedPrice;}
    public void setExpectedPrice(BigDecimal value){expectedPrice=value;}
}
