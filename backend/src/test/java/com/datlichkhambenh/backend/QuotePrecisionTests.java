package com.datlichkhambenh.backend;
import com.datlichkhambenh.backend.dto.response.ConsultationQuoteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class QuotePrecisionTests {
 @Test void configuredRateSurvivesJavaScriptRoundTripAsExactString() throws Exception {
  var dto=new ConsultationQuoteDto(1L,"INSURANCE","Bảo hiểm",new BigDecimal("300000"),new BigDecimal("0.123456789123456789"),new BigDecimal("262963"),"VND",false,java.time.LocalDate.of(2026,11,6),java.time.LocalTime.of(9,0),java.time.LocalTime.of(9,30));
  var data=new ObjectMapper().findAndRegisterModules().readTree(new ObjectMapper().findAndRegisterModules().writeValueAsString(dto));
  assertEquals("0.123456789123456789",data.path("discountRateExact").asText());
 }
}
