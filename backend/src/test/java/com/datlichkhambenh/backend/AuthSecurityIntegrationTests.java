package com.datlichkhambenh.backend;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.Specialty;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.SpecialtyRepository;
import com.datlichkhambenh.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthSecurityIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void publicRegistrationCannotCreateAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"attacker@example.com","password":"Secret123",
                                 "role":"ADMIN","fullName":"Attacker","phone":"0900000000"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void adminApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void specialtiesAreAvailableForDoctorRegistration() throws Exception {
        mockMvc.perform(get("/api/v1/doctors/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void missingFaviconDoesNotProduceServerError() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNoContent());
    }

    @Test
    void doctorsEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changePasswordWithoutAuthIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"oldPassword","newPassword":"newPassword123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientRegistrationWithEmptyDobSucceeds() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"newpatient@test.com","password":"Patient@123",
                                 "role":"PATIENT","fullName":"Bệnh nhân mới","phone":"0981112233",
                                 "dob":""}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testLoginAdminDoctorPatientAccounts() throws Exception {
        // Setup 1 Admin
        User admin = userRepository.save(User.builder()
                .email("admin@clinic.vn")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role("ADMIN")
                .isActive(true)
                .build());

        // Setup 1 Specialty and Doctor
        Specialty spec = specialtyRepository.save(Specialty.builder().name("Tim mạch").description("Chuyên khoa tim").build());
        User docUser = userRepository.save(User.builder()
                .email("bs.nguyenvana@clinic.vn")
                .passwordHash(passwordEncoder.encode("Doctor@123"))
                .role("DOCTOR")
                .isActive(true)
                .build());
        Doctor doc = doctorRepository.save(Doctor.builder()
                .user(docUser)
                .specialty(spec)
                .fullName("BS. Nguyễn Văn An")
                .phone("0901234567")
                .degree("Tiến sĩ")
                .experienceYears(15)
                .consultationFee(BigDecimal.valueOf(350000))
                .isApproved(true)
                .build());

        // Setup 1 Patient
        User patUser = userRepository.save(User.builder()
                .email("bn.hoangminh@gmail.com")
                .passwordHash(passwordEncoder.encode("Patient@123"))
                .role("PATIENT")
                .isActive(true)
                .build());
        Patient pat = patientRepository.save(Patient.builder()
                .user(patUser)
                .fullName("Hoàng Minh Đức")
                .phone("0934567890")
                .build());

        // Test Login Admin -> 200 OK
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@clinic.vn","password":"Admin@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.fullName").value("Quản trị viên"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // Test Login Doctor -> 200 OK
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bs.nguyenvana@clinic.vn","password":"Doctor@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("DOCTOR"))
                .andExpect(jsonPath("$.data.fullName").value("BS. Nguyễn Văn An"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // Test Login Patient -> 200 OK
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bn.hoangminh@gmail.com","password":"Patient@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("PATIENT"))
                .andExpect(jsonPath("$.data.fullName").value("Hoàng Minh Đức"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // Test Login with Wrong Password -> 401 Unauthorized
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@clinic.vn","password":"WrongPassword!"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
