package com.datlichkhambenh.backend.strategy;

import com.datlichkhambenh.backend.entity.*;
import com.datlichkhambenh.backend.repository.*;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class BenefitDecisionIntegrationTests {
 @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired PatientRepository patients;
 @Autowired JwtTokenProvider jwt; @Autowired ObjectMapper json; @Autowired PlatformTransactionManager tm;
 String patient,admin,admin2;long adminId;
 String base="/api/v1/admin/benefit-requests";

 @BeforeEach void seed() {
  String suffix=UUID.randomUUID().toString();
  new TransactionTemplate(tm).executeWithoutResult(tx->{
   User p=users.save(User.builder().email("p-"+suffix+"@test.vn").passwordHash("unused").role("PATIENT").isActive(true).build());
   patients.save(Patient.builder().user(p).fullName("Bệnh nhân duyệt").build());patient=jwt.generateToken(p.getEmail(),"PATIENT");
   User a=users.save(User.builder().email("a-"+suffix+"@test.vn").passwordHash("unused").role("ADMIN").isActive(true).build());
   adminId=a.getId();admin=jwt.generateToken(a.getEmail(),"ADMIN");
   User b=users.save(User.builder().email("b-"+suffix+"@test.vn").passwordHash("unused").role("ADMIN").isActive(true).build());
   admin2=jwt.generateToken(b.getEmail(),"ADMIN");
  });
 }
 long request() throws Exception {
  String path="/api/v1/patients/me/benefit-requests";
  String body="{\"kind\":\"INSURANCE\",\"referenceCode\":\"HS-VERIFY\",\"requestedFrom\":\"2026-01-01\",\"requestedUntil\":\"2027-12-31\"}";
  String data=mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+patient)
   .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  return json.readTree(data).path("data").path("id").asLong();
 }
 String decision(String action) {
  return "{\"action\":\""+action+"\",\"reason\":\"Đã đối chiếu hồ sơ tại phòng khám\",\"approvedFrom\":\"2026-01-01\",\"approvedUntil\":\"2027-12-31\"}";
 }
 int decide(long id,String token,String action) throws Exception {
  String path=base+"/"+id+"/decision";
  return mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+token)
   .contentType(MediaType.APPLICATION_JSON).content(decision(action))).andReturn().getResponse().getStatus();
 }
 @Test void adminApprovesAndPatientSeesVerifiedDatesAndHistory() throws Exception {
  long id=request();assertEquals(200,decide(id,admin,"APPROVE"));
  String path="/api/v1/patients/me/benefit-requests/"+id;
  mvc.perform(get(path).servletPath(path).header("Authorization","Bearer "+patient))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("APPROVED"))
   .andExpect(jsonPath("$.data.approvedFrom").value("2026-01-01"))
   .andExpect(jsonPath("$.data.approvedUntil").value("2027-12-31"))
   .andExpect(jsonPath("$.data.decisions.length()").value(1))
   .andExpect(jsonPath("$.data.decisions[0].action").value("APPROVE"))
   .andExpect(jsonPath("$.data.decisions[0].decidedBy").value(adminId))
   .andExpect(jsonPath("$.data.decisions[0].reason").isNotEmpty());
  mvc.perform(get(base).servletPath(base).header("Authorization","Bearer "+admin)).andExpect(status().isOk());
 }
 @Test void rejectionDoesNotGrantRightsAndCannotBeOverwritten() throws Exception {
  long id=request();assertEquals(200,decide(id,admin,"REJECT"));assertEquals(409,decide(id,admin2,"APPROVE"));
  String path=base+"/"+id;
  mvc.perform(get(path).servletPath(path).header("Authorization","Bearer "+admin))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("REJECTED"))
   .andExpect(jsonPath("$.data.approvedFrom").isEmpty()).andExpect(jsonPath("$.data.decisions.length()").value(1));
 }
 @Test void patientCannotDecideOrReadAdminList() throws Exception {
  long id=request();assertEquals(403,decide(id,patient,"APPROVE"));
  mvc.perform(get(base).servletPath(base).header("Authorization","Bearer "+patient)).andExpect(status().isForbidden());
 }
 @Test void malformedDecisionLeavesRequestPending() throws Exception {
  long id=request();String path=base+"/"+id+"/decision";
  for(String body:List.of("{\"action\":\"APPROVE\",\"reason\":\" \"}", "{\"action\":\"APPROVE\",\"reason\":\"ok\"}",
    "{\"action\":\"APPROVE\",\"reason\":\"ok\",\"approvedFrom\":\"2027-01-01\",\"approvedUntil\":\"2026-01-01\"}")) {
   mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON).content(body))
    .andExpect(status().isBadRequest());
  }
  String detail=base+"/"+id;
  mvc.perform(get(detail).servletPath(detail).header("Authorization","Bearer "+admin)).andExpect(jsonPath("$.data.status").value("PENDING"));
 }
 @Test void twoAdminsCannotMakeConflictingDecisions() throws Exception {
  long id=request();var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
  try {
   Future<Integer> a=pool.submit(()->{start.await();return decide(id,admin,"APPROVE");});
   Future<Integer> b=pool.submit(()->{start.await();return decide(id,admin2,"REJECT");});
   start.countDown();List<Integer> results=new ArrayList<>(List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS)));
   Collections.sort(results);assertEquals(List.of(200,409),results);
   String path=base+"/"+id;
   mvc.perform(get(path).servletPath(path).header("Authorization","Bearer "+admin))
    .andExpect(jsonPath("$.data.decisions.length()").value(1));
  } finally {pool.shutdownNow();}
 }

 @Test void revokeIsAdminOnlyAndKeepsApprovalHistory() throws Exception {
  long id=request();assertEquals(200,decide(id,admin,"APPROVE"));
  String path=base+"/"+id+"/revoke";
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+patient).contentType(MediaType.APPLICATION_JSON)
   .content("{\"reason\":\"Hồ sơ không còn hợp lệ\"}")).andExpect(status().isForbidden());
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
   .content("{\"reason\":\"Hồ sơ không còn hợp lệ\"}")).andExpect(status().isOk())
   .andExpect(jsonPath("$.data.status").value("REVOKED")).andExpect(jsonPath("$.data.decisions.length()").value(2))
   .andExpect(jsonPath("$.data.decisions[0].action").value("APPROVE")).andExpect(jsonPath("$.data.decisions[1].action").value("REVOKE"));
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
   .content("{\"reason\":\"Lặp quyết định\"}")).andExpect(status().isConflict());
 }
 @Test void renewalCreatesNewPendingRequestWithoutOverwritingOriginal() throws Exception {
  long id=request();assertEquals(200,decide(id,admin,"APPROVE"));
  String path="/api/v1/patients/me/benefit-requests";
  String body="{\"kind\":\"INSURANCE\",\"referenceCode\":\"HS-RENEW\",\"requestedFrom\":\"2028-01-01\",\"requestedUntil\":\"2028-12-31\",\"renewalOfId\":"+id+"}";
  String result=mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+patient).contentType(MediaType.APPLICATION_JSON).content(body))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.data.status").value("PENDING"))
   .andExpect(jsonPath("$.data.renewalOfId").value(id)).andReturn().getResponse().getContentAsString();
  assertNotEquals(id,json.readTree(result).path("data").path("id").asLong());
  String original=base+"/"+id;
  mvc.perform(get(original).servletPath(original).header("Authorization","Bearer "+admin))
   .andExpect(jsonPath("$.data.status").value("APPROVED")).andExpect(jsonPath("$.data.decisions.length()").value(1))
   .andExpect(jsonPath("$.data.approvedUntil").value("2027-12-31"));
 }
 @Test void expiredApprovedRightsAreDisplayedAsExpired() throws Exception {
  long id=request();String path=base+"/"+id+"/decision";
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
   .content("{\"action\":\"APPROVE\",\"reason\":\"Hồ sơ cũ\",\"approvedFrom\":\"2020-01-01\",\"approvedUntil\":\"2020-12-31\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("APPROVED"))
   .andExpect(jsonPath("$.data.effectiveStatus").value("EXPIRED"));
 }
 @Test void renewalCannotReferenceAnotherPatientsRequest() throws Exception {
  long id=request();assertEquals(200,decide(id,admin,"APPROVE"));
  String other=new TransactionTemplate(tm).execute(tx->{
   User u=users.save(User.builder().email(UUID.randomUUID()+"@test.vn").passwordHash("unused").role("PATIENT").isActive(true).build());
   patients.save(Patient.builder().user(u).fullName("Other").build());return jwt.generateToken(u.getEmail(),"PATIENT");
  });
  String path="/api/v1/patients/me/benefit-requests";
  mvc.perform(post(path).servletPath(path).header("Authorization","Bearer "+other).contentType(MediaType.APPLICATION_JSON)
   .content("{\"kind\":\"INSURANCE\",\"referenceCode\":\"HS\",\"requestedFrom\":\"2028-01-01\",\"requestedUntil\":\"2028-12-31\",\"renewalOfId\":"+id+"}"))
   .andExpect(status().isNotFound());
 }
}
