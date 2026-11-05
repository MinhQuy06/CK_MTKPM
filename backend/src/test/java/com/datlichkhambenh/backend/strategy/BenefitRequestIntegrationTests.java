package com.datlichkhambenh.backend.strategy;

import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.UserRepository;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class BenefitRequestIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PatientRepository patients;
    @Autowired JwtTokenProvider jwt;
    @Autowired ObjectMapper json;
    private String token;
    private String otherToken;
    private static final String PATH = "/api/v1/patients/me/benefit-requests";

    @BeforeEach void setup() {
        token = patientToken("benefit-patient@test.vn");
        otherToken = patientToken("benefit-other@test.vn");
    }

    private String patientToken(String email) {
        User user = users.save(User.builder().email(email).passwordHash("unused").role("PATIENT").isActive(true).build());
        patients.save(Patient.builder().user(user).fullName("Bệnh nhân quyền hưởng").build());
        return jwt.generateToken(email, "PATIENT");
    }

    private String body(String kind, String reference, LocalDate from, LocalDate until) throws Exception {
        return json.writeValueAsString(Map.of("kind",kind,"referenceCode",reference,"requestedFrom",from.toString(),"requestedUntil",until.toString()));
    }

    private String valid() throws Exception {
        return body("INSURANCE","HS-001", LocalDate.now(), LocalDate.now().plusMonths(6));
    }

    @Test void patientSubmitsPendingRequestAndReadsItBack() throws Exception {
        String response = mvc.perform(post(PATH).servletPath(PATH).header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON).content(valid()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.kind").value("INSURANCE"))
                .andExpect(jsonPath("$.data.referenceCode").value("HS-001")).andReturn().getResponse().getContentAsString();
        long id=json.readTree(response).path("data").path("id").asLong();
        mvc.perform(get(PATH).servletPath(PATH).header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(id));
        String detail=PATH+"/"+id;
        mvc.perform(get(detail).servletPath(detail).header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));
        mvc.perform(get(detail).servletPath(detail).header("Authorization","Bearer "+otherToken))
                .andExpect(status().isNotFound());
        mvc.perform(get(PATH).servletPath(PATH).header("Authorization","Bearer "+otherToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test void clientCannotSelfApproveOrAssignAnotherPatient() throws Exception {
        String forged=valid().replace("\"kind\":", "\"status\":\"APPROVED\",\"patientId\":999999,\"approvedBy\":1,\"kind\":");
        mvc.perform(post(PATH).servletPath(PATH).header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON).content(forged))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.status").value("PENDING"));
        mvc.perform(get(PATH).servletPath(PATH).header("Authorization","Bearer "+otherToken))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @ParameterizedTest @ValueSource(strings={"STANDARD","UNKNOWN","", "insurance"})
    void unsupportedBenefitKindIsRejected(String kind) throws Exception {
        mvc.perform(post(PATH).servletPath(PATH).header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON).content(body(kind,"HS",LocalDate.now(),LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test void reversedDatesAndBlankReferenceAreRejected() throws Exception {
        mvc.perform(post(PATH).servletPath(PATH).header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON).content(body("VIP","HS",LocalDate.now(),LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest());
        mvc.perform(post(PATH).servletPath(PATH).header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON).content(body("VIP"," ",LocalDate.now(),LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test void anonymousCannotSubmitOrRead() throws Exception {
        mvc.perform(get(PATH).servletPath(PATH)).andExpect(status().isUnauthorized());
        mvc.perform(post(PATH).servletPath(PATH).contentType(MediaType.APPLICATION_JSON).content(valid()))
                .andExpect(status().isUnauthorized());
    }

    @Test void adminCannotSubmitPatientRequest() throws Exception {
        users.save(User.builder().email("benefit-admin@test.vn").passwordHash("unused").role("ADMIN").isActive(true).build());
        mvc.perform(post(PATH).servletPath(PATH).header("Authorization","Bearer "+jwt.generateToken("benefit-admin@test.vn","ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(valid())).andExpect(status().isForbidden());
    }
}
