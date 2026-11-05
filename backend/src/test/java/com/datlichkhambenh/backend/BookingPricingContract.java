package com.datlichkhambenh.backend;

import com.datlichkhambenh.backend.entity.*;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import com.datlichkhambenh.backend.repository.*;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@Import(BookingPricingContract.EmailCapture.class)
abstract class BookingPricingContract {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired JwtTokenProvider jwt;
 @Autowired UserRepository users; @Autowired PatientRepository patients; @Autowired DoctorRepository doctors;
 @Autowired SpecialtyRepository specialties; @Autowired ScheduleRepository schedules; @Autowired PlatformTransactionManager tm;
 @Autowired CapturingRabbitTemplate sink;
 Long scheduleId,doctorId,patientId;String patient,otherPatient;LocalDate day;
 static final String PATH="/api/v1/appointments";
 @TestConfiguration static class EmailCapture {
  @Bean @Primary CapturingRabbitTemplate capturedRabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory factory){return new CapturingRabbitTemplate(factory);}
 }
 static class CapturingRabbitTemplate extends RabbitTemplate {
  CapturingRabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory factory){super(factory);}
  final BlockingQueue<EmailNotificationMessage> messages=new LinkedBlockingQueue<>();
  @Override public void convertAndSend(String exchange,String routingKey,Object message) {
   messages.add((EmailNotificationMessage)message);
  }
 }
 @BeforeEach void seed() {
  sink.messages.clear();String suffix=UUID.randomUUID().toString();day=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(2);
  new TransactionTemplate(tm).executeWithoutResult(tx->{
   User p=users.save(User.builder().email("booking-p-"+suffix+"@test.vn").passwordHash("unused").role("PATIENT").isActive(true).build());
   patients.save(Patient.builder().user(p).fullName("Booking Patient").build());patientId=p.getId();patient=jwt.generateToken(p.getEmail(),"PATIENT");
   User o=users.save(User.builder().email("booking-o-"+suffix+"@test.vn").passwordHash("unused").role("PATIENT").isActive(true).build());
   patients.save(Patient.builder().user(o).fullName("Other Patient").build());otherPatient=jwt.generateToken(o.getEmail(),"PATIENT");
   User d=users.save(User.builder().email("booking-d-"+suffix+"@test.vn").passwordHash("unused").role("DOCTOR").isActive(true).build());
   var specialty=specialties.save(Specialty.builder().name("Booking "+suffix).build());
   var doctor=doctors.save(Doctor.builder().user(d).specialty(specialty).fullName("Booking Doctor").consultationFee(new BigDecimal("300000")).isApproved(true).build());doctorId=doctor.getUserId();
   scheduleId=schedules.save(Schedule.builder().doctor(doctor).workDate(day).startTime(LocalTime.of(9,0)).endTime(LocalTime.of(9,30)).isAvailable(true).build()).getId();
  });
 }
 Map<String,Object> confirmed() {
  Map<String,Object> body=new HashMap<>();body.put("scheduleId",scheduleId);body.put("symptoms","Giữ triệu chứng");
  body.put("strategyKey","STANDARD");body.put("expectedBasePrice",300000);body.put("expectedDiscountRate",0);body.put("expectedPrice",300000);return body;
 }
 org.springframework.test.web.servlet.MvcResult book(Map<String,Object> body,String token) throws Exception {
  return mvc.perform(post(PATH+"/confirm").servletPath(PATH+"/confirm").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
   .content(json.writeValueAsString(body))).andReturn();
 }
 void noBookingOrEmail() throws Exception {
  String path=PATH+"/my-appointments";
  mvc.perform(get(path).servletPath(path).header("Authorization","Bearer "+patient)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
  assertNull(sink.messages.poll(300,TimeUnit.MILLISECONDS));
 }
 EmailNotificationMessage email() throws Exception {
  var message=sink.messages.poll(3,TimeUnit.SECONDS);assertNotNull(message,"after-commit notification missing");return message;
 }
 @Test void standardBookingStoresSnapshotAndNotificationUsesStoredPrice() throws Exception {
  var response=book(confirmed(),patient);assertEquals(201,response.getResponse().getStatus());
  var data=json.readTree(response.getResponse().getContentAsString()).path("data");
  assertEquals("STANDARD",data.path("pricingKey").asText());assertEquals(300000,data.path("basePrice").asInt());assertEquals(0,data.path("discountRate").decimalValue().signum());
  assertEquals(300000,data.path("price").asInt());
  var message=email();assertEquals(0,new BigDecimal(message.price()).compareTo(new BigDecimal("300000")));
  new TransactionTemplate(tm).executeWithoutResult(tx->{var d=doctors.findById(doctorId).orElseThrow();d.setConsultationFee(new BigDecimal("500000"));doctors.save(d);});
  String path=PATH+"/"+data.path("id").asLong();
  mvc.perform(get(path).servletPath(path).header("Authorization","Bearer "+patient)).andExpect(jsonPath("$.data.price").value(300000)).andExpect(jsonPath("$.data.basePrice").value(300000));
 }
 @Test void forgedPriceCannotCreateBookingAndDoesNotConsumeSlot() throws Exception {
  var body=confirmed();body.put("expectedPrice",1);
  var result=book(body,patient);assertEquals(409,result.getResponse().getStatus());
  assertEquals("PRICE_CHANGED",json.readTree(result.getResponse().getContentAsString()).path("code").asText());noBookingOrEmail();
  var retry=book(confirmed(),patient);assertEquals(201,retry.getResponse().getStatus());email();
 }
 @Test void changedDoctorFeeRequiresReconfirmationAndRollbackHasNoEmail() throws Exception {
  new TransactionTemplate(tm).executeWithoutResult(tx->{var d=doctors.findById(doctorId).orElseThrow();d.setConsultationFee(new BigDecimal("350000"));doctors.save(d);});
  var response=book(confirmed(),patient);assertEquals(409,response.getResponse().getStatus());noBookingOrEmail();
 }
 @Test void newFlowCannotOmitExpectedQuoteAndUnknownKeyIsRejected() throws Exception {
  var body=confirmed();body.remove("expectedPrice");assertEquals(400,book(body,patient).getResponse().getStatus());noBookingOrEmail();
  body=confirmed();body.put("strategyKey","UNKNOWN");assertEquals(400,book(body,patient).getResponse().getStatus());noBookingOrEmail();
 }
 @Test void legacyClientDefaultsToStandardButNewSnapshotIsStillStored() throws Exception {
  var result=mvc.perform(post(PATH).servletPath(PATH).header("Authorization","Bearer "+patient).contentType(MediaType.APPLICATION_JSON)
   .content(json.writeValueAsString(Map.of("scheduleId",scheduleId,"symptoms","legacy")))).andReturn();assertEquals(201,result.getResponse().getStatus());
  assertEquals("STANDARD",json.readTree(result.getResponse().getContentAsString()).path("data").path("pricingKey").asText());email();
 }
 @Test void onlyOneConcurrentPatientGetsTheSlot() throws Exception {
  var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
  try {
   var a=pool.submit(()->{start.await();return book(confirmed(),patient).getResponse().getStatus();});
   var b=pool.submit(()->{start.await();return book(confirmed(),otherPatient).getResponse().getStatus();});
   start.countDown();var result=new ArrayList<>(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS)));Collections.sort(result);assertEquals(List.of(201,409),result);email();assertNull(sink.messages.poll(300,TimeUnit.MILLISECONDS));
  }finally{pool.shutdownNow();}
 }

 @Test void confirmRouteCannotBeDowngradedToLegacyByRemovingQuoteFields() throws Exception {
  String path=PATH+"/confirm";
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+patient).contentType(MediaType.APPLICATION_JSON)
   .content(json.writeValueAsString(Map.of("scheduleId",scheduleId,"quoteRequired",false))))
   .andExpect(status().isBadRequest());noBookingOrEmail();
 }
}
