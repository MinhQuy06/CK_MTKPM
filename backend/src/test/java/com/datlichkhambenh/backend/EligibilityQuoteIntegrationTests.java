package com.datlichkhambenh.backend;
import com.datlichkhambenh.backend.entity.*;
import com.datlichkhambenh.backend.repository.*;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class EligibilityQuoteIntegrationTests {
 @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired PatientRepository patients;
 @Autowired DoctorRepository doctors; @Autowired SpecialtyRepository specialties; @Autowired ScheduleRepository schedules;
 @Autowired BenefitRequestRepository benefits; @Autowired JwtTokenProvider jwt;
 Patient patient; Schedule schedule; String token;
 LocalDate today=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
 @BeforeEach void fixture() {
  User pu=users.save(User.builder().email("elig-patient@test.vn").passwordHash("unused").role("PATIENT").isActive(true).build());
  patient=patients.save(Patient.builder().user(pu).fullName("Eligibility Patient").build());token=jwt.generateToken(pu.getEmail(),"PATIENT");
  User du=users.save(User.builder().email("elig-doctor@test.vn").passwordHash("unused").role("DOCTOR").isActive(true).build());
  var specialty=specialties.save(Specialty.builder().name("Eligibility").build());
  var doctor=doctors.save(Doctor.builder().user(du).specialty(specialty).fullName("Doctor").consultationFee(new BigDecimal("300000")).isApproved(true).build());
  schedule=schedules.save(Schedule.builder().doctor(doctor).workDate(today.plusDays(1)).startTime(LocalTime.of(9,0)).endTime(LocalTime.of(9,30)).isAvailable(true).build());
 }
 BenefitRequest grant(String kind,String status,int from,int until) {
  var r=new BenefitRequest();r.setPatient(patient);r.setKind(kind);r.setReferenceCode("ELIG-HS");r.setRequestedFrom(today.plusDays(from));r.setRequestedUntil(today.plusDays(until));
  r.setApprovedFrom(today.plusDays(from));r.setApprovedUntil(today.plusDays(until));r.setStatus(status);return benefits.saveAndFlush(r);
 }
 org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder quote(String kind) {
  String p="/api/v1/appointments/quote";return get(p).servletPath(p).param("scheduleId",schedule.getId().toString()).param("strategyKey",kind).header("Authorization","Bearer "+token);
 }
 @Test void approvedInsuranceQuotesTwentyPercentAndIsListedAsEligible() throws Exception {
  grant("INSURANCE","APPROVED",0,1);
  mvc.perform(quote("INSURANCE")).andExpect(status().isOk()).andExpect(jsonPath("$.data.finalPrice").value(60000))
   .andExpect(jsonPath("$.data.discountRate").value(.8)).andExpect(jsonPath("$.data.simulated").value(false));
  String p="/api/v1/appointments/pricing-options";
  mvc.perform(get(p).servletPath(p).param("scheduleId",schedule.getId().toString()).header("Authorization","Bearer "+token))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data[*].key",hasItem("INSURANCE"))).andExpect(jsonPath("$.data[0].key").value("STANDARD"));
 }
 @ParameterizedTest @CsvSource({"0,1,APPROVED,200","-1,0,APPROVED,403","1,2,APPROVED,403","-1,2,PENDING,403","-1,2,REJECTED,403","-1,2,REVOKED,403","-2,-1,APPROVED,403"})
 void rightsMustBeApprovedAndCoverTodayAndAppointmentDay(int from,int until,String state,int expected) throws Exception {
  grant("INSURANCE",state,from,until);mvc.perform(quote("INSURANCE")).andExpect(status().is(expected));
 }
 @Test void unverifiedInsuranceIsDeniedAndOnlyStandardIsOffered() throws Exception {
  mvc.perform(quote("INSURANCE")).andExpect(status().isForbidden());
  String p="/api/v1/appointments/pricing-options";
  mvc.perform(get(p).servletPath(p).param("scheduleId",schedule.getId().toString()).header("Authorization","Bearer "+token))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].key").value("STANDARD"));
 }
 @Test void revocationRemovesQuoteEligibilityImmediately() throws Exception {
  var right=grant("INSURANCE","APPROVED",-1,2);mvc.perform(quote("INSURANCE")).andExpect(status().isOk());
  right.setStatus("REVOKED");benefits.saveAndFlush(right);mvc.perform(quote("INSURANCE")).andExpect(status().isForbidden());
 }
 @Test void anotherPatientsInsuranceCannotBeUsed() throws Exception {
  var right=grant("INSURANCE","APPROVED",-1,2);
  var other=users.save(User.builder().email("elig-other@test.vn").passwordHash("unused").role("PATIENT").isActive(true).build());
  right.setPatient(patients.save(Patient.builder().user(other).fullName("Other").build()));benefits.saveAndFlush(right);
  mvc.perform(quote("INSURANCE")).andExpect(status().isForbidden());
 }
}
