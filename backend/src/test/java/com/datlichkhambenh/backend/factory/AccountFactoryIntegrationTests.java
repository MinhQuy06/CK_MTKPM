package com.datlichkhambenh.backend.factory;

import com.datlichkhambenh.backend.entity.Specialty;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.SpecialtyRepository;
import com.datlichkhambenh.backend.repository.UserRepository;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Factory tickets 19-22: every way of creating a clinic account, seen through the API. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountFactoryIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PatientRepository patients;
    @Autowired DoctorRepository doctors;
    @Autowired SpecialtyRepository specialties;
    @Autowired JwtTokenProvider jwt;

    ResultActions postJson(String path, String body, String token) throws Exception {
        var request = post(path).servletPath(path).contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(request);
    }

    void canLogIn(String email, String password, String role) throws Exception {
        postJson("/api/v1/auth/login", """
                {"email":"%s","password":"%s"}""".formatted(email, password), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value(role));
    }

    @Test
    void patientRegistrationCreatesALoginAndAPatientProfile() throws Exception {
        postJson("/api/v1/auth/register", """
                {"email":"Factory.Patient@Test.vn","password":"Patient@123","role":"PATIENT",
                 "fullName":"BN Factory","phone":"0981234001","gender":"FEMALE","address":"Hà Nội"}""", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("PATIENT"));

        canLogIn("factory.patient@test.vn", "Patient@123", "PATIENT");
        User user = users.findByEmailIgnoreCase("factory.patient@test.vn").orElseThrow();
        var patient = patients.findById(user.getId()).orElseThrow();
        assertEquals("BN Factory", patient.getFullName());
        assertEquals("0981234001", patient.getPhone());
        assertEquals("Hà Nội", patient.getAddress());
    }

    Specialty specialty() {
        return specialties.save(Specialty.builder().name("Factory khoa " + System.nanoTime()).build());
    }

    String adminToken() {
        User admin = users.save(User.builder().email("factory-admin@test.vn").passwordHash("unused")
                .role("ADMIN").isActive(true).build());
        return jwt.generateToken(admin.getEmail(), "ADMIN");
    }

    @Test
    void doctorRegistrationWaitsForAdminApproval() throws Exception {
        Long specialtyId = specialty().getId();

        postJson("/api/v1/auth/register", """
                {"email":"bs.factory@test.vn","password":"Doctor@123","role":"DOCTOR","fullName":"BS Factory",
                 "phone":"0981234002","specialtyId":%d,"degree":"ThS","experienceYears":5,"consultationFee":250000}"""
                .formatted(specialtyId), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("DOCTOR"));

        String pending = "/api/v1/admin/doctors/pending";
        mvc.perform(get(pending).servletPath(pending).header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.fullName == 'BS Factory')]").exists());
        var doctor = doctors.findById(users.findByEmailIgnoreCase("bs.factory@test.vn").orElseThrow().getId()).orElseThrow();
        assertEquals(false, doctor.getIsApproved());
        assertEquals(specialtyId, doctor.getSpecialty().getId());
        assertEquals(0, doctor.getConsultationFee().compareTo(new java.math.BigDecimal("250000")));
    }

    @Test
    void adminCreatesAnApprovedDoctorWhoCanLogIn() throws Exception {
        Long specialtyId = specialty().getId();

        postJson("/api/v1/admin/doctors", """
                {"email":"BS.Admin@Test.vn","password":"Doctor@1234","fullName":"  BS Admin  ","phone":"0981234004",
                 "specialtyId":%d,"experienceYears":3,"consultationFee":200000}""".formatted(specialtyId), adminToken())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fullName").value("BS Admin"));

        canLogIn("bs.admin@test.vn", "Doctor@1234", "DOCTOR");
        var doctor = doctors.findById(users.findByEmailIgnoreCase("bs.admin@test.vn").orElseThrow().getId()).orElseThrow();
        assertEquals(true, doctor.getIsApproved());
    }

    @Test
    void adminMayCreateADoctorStillWaitingForApproval() throws Exception {
        postJson("/api/v1/admin/doctors", """
                {"email":"bs.cho@test.vn","password":"Doctor@1234","fullName":"BS Chờ","phone":"0981234005",
                 "specialtyId":%d,"approved":false}""".formatted(specialty().getId()), adminToken())
                .andExpect(status().isCreated());

        var doctor = doctors.findById(users.findByEmailIgnoreCase("bs.cho@test.vn").orElseThrow().getId()).orElseThrow();
        assertEquals(false, doctor.getIsApproved());
    }

    @Test
    void adminCreatesAPatientWhoCanLogIn() throws Exception {
        postJson("/api/v1/admin/patients", """
                {"email":"bn.quay@test.vn","password":"Patient@1234","fullName":"  BN Quầy  ","phone":"0981234006",
                 "gender":"MALE"}""", adminToken())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fullName").value("BN Quầy"));

        canLogIn("bn.quay@test.vn", "Patient@1234", "PATIENT");
    }

    @Test
    void doctorRegistrationWithoutSpecialtyIsRejectedAsBefore() throws Exception {
        postJson("/api/v1/auth/register", """
                {"email":"bs.nokhoa@test.vn","password":"Doctor@123","role":"DOCTOR","fullName":"BS Không khoa",
                 "phone":"0981234003","experienceYears":5,"consultationFee":250000}""", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bác sĩ phải đăng ký thuộc 1 chuyên khoa (specialtyId)"));
    }
}
