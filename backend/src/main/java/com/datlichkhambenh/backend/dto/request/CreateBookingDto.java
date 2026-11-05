package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class CreateBookingDto {

    @NotNull(message = "scheduleId không được để trống")
    private Long scheduleId;

    private String symptoms;
    private java.time.LocalDate expectedWorkDate;
    private java.time.LocalTime expectedStartTime, expectedEndTime;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private boolean quoteRequired;
    private String strategyKey;
    private java.math.BigDecimal expectedBasePrice;
    private java.math.BigDecimal expectedDiscountRate;
    private java.math.BigDecimal expectedPrice;

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
        private java.time.LocalDate expectedWorkDate;
        private java.time.LocalTime expectedStartTime, expectedEndTime;
        private java.math.BigDecimal expectedBasePrice, expectedDiscountRate, expectedPrice;

        public CreateBookingDtoBuilder scheduleId(Long scheduleId) { this.scheduleId = scheduleId; return this; }
        public CreateBookingDtoBuilder symptoms(String symptoms) { this.symptoms = symptoms; return this; }

        public CreateBookingDtoBuilder expectedWorkDate(java.time.LocalDate value){this.expectedWorkDate=value;return this;}
        public CreateBookingDtoBuilder expectedStartTime(java.time.LocalTime value){this.expectedStartTime=value;return this;}
        public CreateBookingDtoBuilder expectedEndTime(java.time.LocalTime value){this.expectedEndTime=value;return this;}
        public CreateBookingDtoBuilder strategyKey(String value){this.strategyKey=value;return this;}
        public CreateBookingDtoBuilder expectedBasePrice(java.math.BigDecimal value){this.expectedBasePrice=value;return this;}
        public CreateBookingDtoBuilder expectedDiscountRate(java.math.BigDecimal value){this.expectedDiscountRate=value;return this;}
        public CreateBookingDtoBuilder expectedPrice(java.math.BigDecimal value){this.expectedPrice=value;return this;}
        public CreateBookingDto build() {
            var dto=new CreateBookingDto(scheduleId,symptoms);dto.strategyKey=strategyKey;dto.expectedBasePrice=expectedBasePrice;
            dto.expectedWorkDate=expectedWorkDate;dto.expectedStartTime=expectedStartTime;dto.expectedEndTime=expectedEndTime;
            dto.expectedDiscountRate=expectedDiscountRate;dto.expectedPrice=expectedPrice;return dto;
        }
    }

    public java.time.LocalDate getExpectedWorkDate(){return expectedWorkDate;}
    public void setExpectedWorkDate(java.time.LocalDate value){expectedWorkDate=value;}
    public java.time.LocalTime getExpectedStartTime(){return expectedStartTime;}
    public void setExpectedStartTime(java.time.LocalTime value){expectedStartTime=value;}
    public java.time.LocalTime getExpectedEndTime(){return expectedEndTime;}
    public void setExpectedEndTime(java.time.LocalTime value){expectedEndTime=value;}

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getStrategyKey(){return strategyKey;}
    public void setStrategyKey(String value){strategyKey=value;}
    public java.math.BigDecimal getExpectedBasePrice(){return expectedBasePrice;}
    public void setExpectedBasePrice(java.math.BigDecimal value){expectedBasePrice=value;}
    public java.math.BigDecimal getExpectedDiscountRate(){return expectedDiscountRate;}
    public void setExpectedDiscountRate(java.math.BigDecimal value){expectedDiscountRate=value;}
    public java.math.BigDecimal getExpectedPrice(){return expectedPrice;}
    public void setExpectedPrice(java.math.BigDecimal value){expectedPrice=value;}
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isQuoteRequired(){return quoteRequired;}
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setQuoteRequired(boolean value){quoteRequired=value;}
}
