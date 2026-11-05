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
   User adminUser=users.save(User.builder().email("booking-a-"+suffix+"@test.vn").passwordHash("unused").role("ADMIN").isActive(true).build());admin=jwt.generateToken(adminUser.getEmail(),"ADMIN");
   User d=users.save(User.builder().email("booking-d-"+suffix+"@test.vn").passwordHash("unused").role("DOCTOR").isActive(true).build());
   var specialty=specialties.save(Specialty.builder().name("Booking "+suffix).build());
   var doctor=doctors.save(Doctor.builder().user(d).specialty(specialty).fullName("Booking Doctor").consultationFee(new BigDecimal("300000")).isApproved(true).build());doctorId=doctor.getUserId();
   scheduleId=schedules.save(Schedule.builder().doctor(doctor).workDate(day).startTime(LocalTime.of(9,0)).endTime(LocalTime.of(9,30)).isAvailable(true).build()).getId();
  });
 }
 Map<String,Object> confirmed() {
  Map<String,Object> body=new HashMap<>();body.put("scheduleId",scheduleId);body.put("symptoms","Giữ triệu chứng");
  body.put("expectedWorkDate",day.toString());body.put("expectedStartTime","09:00:00");body.put("expectedEndTime","09:30:00");
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

 @Autowired BenefitRequestRepository benefitRequests;
 String admin;
 Long grant(String kind,LocalDate from,LocalDate until) throws Exception {
  String path="/api/v1/patients/me/benefit-requests";
  var payload=Map.of("kind",kind,"referenceCode","BOOK-GRANT","requestedFrom",from.toString(),"requestedUntil",until.toString());
  var response=mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+patient).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload)))
    .andExpect(status().isCreated()).andReturn();
  long id=json.readTree(response.getResponse().getContentAsString()).path("data").path("id").asLong();
  path="/api/v1/admin/benefit-requests/"+id+"/decision";
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
    .content(json.writeValueAsString(Map.of("action","APPROVE","reason","Đã xác minh","approvedFrom",from.toString(),"approvedUntil",until.toString()))))
    .andExpect(status().isOk());
  return id;
 }
 void revoke(Long id) throws Exception {
  String path="/api/v1/admin/benefit-requests/"+id+"/revoke";
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
   .content("{\"reason\":\"Quyền không còn hợp lệ\"}")).andExpect(status().isOk());
 }
 Map<String,Object> discounted(String kind) {
  var body=confirmed();body.put("strategyKey",kind);body.put("expectedDiscountRate",new BigDecimal(kind.equals("INSURANCE")?"0.8":"0.1"));
  body.put("expectedPrice",kind.equals("INSURANCE")?60000:270000);return body;
 }
 @org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"INSURANCE","VIP"})
 void discountedBookingPersistsGrantAndKeepsPriceAfterRevocation(String kind) throws Exception {
  long grant=grant(kind,day.minusDays(5),day.plusDays(30));
  var result=book(discounted(kind),patient);assertEquals(201,result.getResponse().getStatus());
  var data=json.readTree(result.getResponse().getContentAsString()).path("data");
  assertEquals(grant,data.path("benefitRequestId").asLong());assertEquals(kind,data.path("pricingKey").asText());
  int expected=kind.equals("INSURANCE")?60000:270000;assertEquals(expected,data.path("price").asInt());
  assertEquals(0,new BigDecimal(email().price()).compareTo(new BigDecimal(expected)));
  revoke(grant);String detail=PATH+"/"+data.path("id").asLong();
  mvc.perform(get(detail).servletPath(detail).header("Authorization","Bearer "+patient)).andExpect(jsonPath("$.data.price").value(expected)).andExpect(jsonPath("$.data.benefitRequestId").value(grant));
 }
 @Test void revokedGrantCannotBookOrConsumeSlot() throws Exception {
  long grant=grant("INSURANCE",day.minusDays(5),day.plusDays(30));revoke(grant);
  var response=book(discounted("INSURANCE"),patient);assertEquals(403,response.getResponse().getStatus());
  assertEquals("BENEFIT_UNAVAILABLE",json.readTree(response.getResponse().getContentAsString()).path("code").asText());
  noBookingOrEmail();assertEquals(201,book(confirmed(),patient).getResponse().getStatus());email();
 }
 @Test void expiredGrantIsNotEnoughEvenWhenClientKnowsDiscountRate() throws Exception {
  grant("VIP",day.minusYears(2),day.minusYears(1));
  assertEquals(403,book(discounted("VIP"),patient).getResponse().getStatus());noBookingOrEmail();
 }
 @Test void bookingWaitsForInFlightRevocationAndRejectsAfterCommit() throws Exception {
  long id=grant("INSURANCE",day.minusDays(5),day.plusDays(30));
  var pool=Executors.newSingleThreadExecutor();var holder=new java.util.concurrent.atomic.AtomicReference<Future<org.springframework.test.web.servlet.MvcResult>>();
  try {
   new TransactionTemplate(tm).executeWithoutResult(tx->{
    benefitRequests.findForUpdate(id).orElseThrow();
    try {
     revoke(id);
     holder.set(pool.submit(()->book(discounted("INSURANCE"),patient)));
     assertThrows(TimeoutException.class,()->holder.get().get(300,TimeUnit.MILLISECONDS),"booking must wait for pending revocation");
    }catch(Exception e){throw new RuntimeException(e);}
   });
   var response=holder.get().get(20,TimeUnit.SECONDS);assertEquals(403,response.getResponse().getStatus());noBookingOrEmail();
  }finally{pool.shutdownNow();}
 }

 @Autowired jakarta.persistence.EntityManager entityManager;
 @Test void bookingWaitsForFeeUpdateAndRequiresReconfirmation() throws Exception {
  var pool=Executors.newSingleThreadExecutor();var holder=new java.util.concurrent.atomic.AtomicReference<Future<org.springframework.test.web.servlet.MvcResult>>();
  try {
   new TransactionTemplate(tm).executeWithoutResult(tx->{
    var doctor=entityManager.find(Doctor.class,doctorId,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    doctor.setConsultationFee(new BigDecimal("350000"));doctors.saveAndFlush(doctor);
    holder.set(pool.submit(()->book(confirmed(),patient)));
    assertThrows(TimeoutException.class,()->holder.get().get(300,TimeUnit.MILLISECONDS));
   });
   var response=holder.get().get(20,TimeUnit.SECONDS);assertEquals(409,response.getResponse().getStatus());noBookingOrEmail();
  }finally{pool.shutdownNow();}
 }

 @Test void shiftedQuotedSlotRequiresFreshSelectionBeforeBooking() throws Exception {
  new TransactionTemplate(tm).executeWithoutResult(tx->{var s=schedules.findById(scheduleId).orElseThrow();s.setWorkDate(day.plusDays(1));s.setStartTime(LocalTime.of(10,0));s.setEndTime(LocalTime.of(10,30));schedules.save(s);});
  var response=book(confirmed(),patient);assertEquals(409,response.getResponse().getStatus());
  assertEquals("SLOT_UNAVAILABLE",json.readTree(response.getResponse().getContentAsString()).path("code").asText());noBookingOrEmail();
  var refreshed=confirmed();refreshed.put("expectedWorkDate",day.plusDays(1).toString());refreshed.put("expectedStartTime","10:00:00");refreshed.put("expectedEndTime","10:30:00");
  var saved=book(refreshed,patient);assertEquals(201,saved.getResponse().getStatus());
  var data=json.readTree(saved.getResponse().getContentAsString()).path("data");assertEquals(day.plusDays(1).toString(),data.path("workDate").asText());assertEquals("10:00:00",data.path("startTime").asText());email();
 }
 @Test void strictConfirmationCannotStripQuotedSlotTiming() throws Exception {
  var body=confirmed();body.remove("expectedWorkDate");body.remove("expectedStartTime");body.remove("expectedEndTime");
  var response=book(body,patient);assertEquals(400,response.getResponse().getStatus());
  assertEquals("QUOTE_REQUIRED",json.readTree(response.getResponse().getContentAsString()).path("code").asText());noBookingOrEmail();
 }
 @Test void deletedQuotedSlotReturnsRecoveryCodeWithoutBookingOrEmail() throws Exception {
  new TransactionTemplate(tm).executeWithoutResult(tx->schedules.deleteById(scheduleId));
  var response=book(confirmed(),patient);assertEquals(404,response.getResponse().getStatus());
  assertEquals("SLOT_UNAVAILABLE",json.readTree(response.getResponse().getContentAsString()).path("code").asText());
  assertFalse(schedules.existsById(scheduleId));assertNull(sink.messages.poll(500,TimeUnit.MILLISECONDS));
  String path=PATH+"/quote";
  mvc.perform(get(path).servletPath(path).header("Authorization","Bearer "+patient).param("scheduleId",scheduleId.toString()))
    .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SLOT_UNAVAILABLE"));
 }
 @Test void stalePolicyRateRequiresFreshQuoteEvenWhenBaseUnchanged() throws Exception {
  grant("INSURANCE",day.minusDays(5),day.plusDays(30));
  var stale=discounted("INSURANCE");stale.put("expectedDiscountRate",new BigDecimal("0.7"));stale.put("expectedPrice",90000);
  var rejected=book(stale,patient);assertEquals(409,rejected.getResponse().getStatus());
  assertEquals("PRICE_CHANGED",json.readTree(rejected.getResponse().getContentAsString()).path("code").asText());noBookingOrEmail();
  assertEquals(201,book(discounted("INSURANCE"),patient).getResponse().getStatus());assertEquals(0,new BigDecimal(email().price()).compareTo(new BigDecimal("60000")));
 }
 @Test void repeatedFeeChangesRequireEachFreshConfirmation() throws Exception {
  new TransactionTemplate(tm).executeWithoutResult(tx->{var d=doctors.findById(doctorId).orElseThrow();d.setConsultationFee(new BigDecimal("350000"));doctors.save(d);});
  assertEquals(409,book(confirmed(),patient).getResponse().getStatus());noBookingOrEmail();
  var second=confirmed();second.put("expectedBasePrice",350000);second.put("expectedPrice",350000);
  new TransactionTemplate(tm).executeWithoutResult(tx->{var d=doctors.findById(doctorId).orElseThrow();d.setConsultationFee(new BigDecimal("360000"));doctors.save(d);});
  assertEquals(409,book(second,patient).getResponse().getStatus());noBookingOrEmail();
  second.put("expectedBasePrice",360000);second.put("expectedPrice",360000);
  var response=book(second,patient);assertEquals(201,response.getResponse().getStatus());
  assertEquals(360000,json.readTree(response.getResponse().getContentAsString()).path("data").path("price").asInt());email();
 }
}
