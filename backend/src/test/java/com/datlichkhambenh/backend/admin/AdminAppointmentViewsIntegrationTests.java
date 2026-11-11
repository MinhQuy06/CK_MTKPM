package com.datlichkhambenh.backend.admin;

import com.datlichkhambenh.backend.entity.MedicalRecord;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.repository.MedicalRecordRepository;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/** Dữ liệu thật thay cho số liệu giả trên giao diện: lịch theo bác sĩ, thống kê, chẩn đoán, phí khám. */
class AdminAppointmentViewsIntegrationTests extends BookingApiFixture {
    @Autowired AppointmentRepository appointments;
    @Autowired MedicalRecordRepository records;

    private String adminToken;

    @BeforeEach
    void admin() {
        String email = "pattern-a-" + UUID.randomUUID() + "@test.vn";
        users.save(User.builder().email(email).passwordHash("unused").role("ADMIN").isActive(true).build());
        adminToken = jwt.generateToken(email, "ADMIN");
    }

    private JsonNode getJson(String path, String token) throws Exception {
        String servletPath = path.contains("?") ? path.substring(0, path.indexOf('?')) : path;
        var response = mvc.perform(get(path).servletPath(servletPath).header("Authorization", "Bearer " + token))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).path("data");
    }

    private long bookWithDiagnosis() throws Exception {
        long id = book().path("id").asLong();
        new TransactionTemplate(tm).executeWithoutResult(tx -> records.save(MedicalRecord.builder()
                .appointment(appointments.findById(id).orElseThrow())
                .diagnosis("Viêm họng cấp").doctorNotes("Súc miệng nước muối").createdAt(LocalDateTime.now()).build()));
        return id;
    }

    @Test
    void adminSeesRealAppointmentsOfADoctorWithDiagnosis() throws Exception {
        long id = bookWithDiagnosis();

        JsonNode list = getJson("/api/v1/admin/doctors/" + doctorId + "/appointments", adminToken);

        assertEquals(1, list.size());
        assertEquals(id, list.get(0).path("id").asLong());
        assertEquals("Viêm họng cấp", list.get(0).path("diagnosis").asText());
        assertEquals("Súc miệng nước muối", list.get(0).path("doctorNotes").asText());
    }

    @Test
    void appointmentStatsCountRealAppointmentsPerDoctor() throws Exception {
        book();

        JsonNode stats = getJson("/api/v1/admin/doctors/appointment-stats", adminToken);

        JsonNode mine = null;
        for (JsonNode s : stats) {
            if (s.path("doctorId").asLong() == doctorId) mine = s;
        }
        assertTrue(mine != null, stats.toString());
        assertEquals(1, mine.path("total").asLong());
        assertEquals(1, mine.path("confirmed").asLong());
    }

    @Test
    void patientAppointmentListCarriesTheDiagnosis() throws Exception {
        bookWithDiagnosis();

        JsonNode mine = getJson(PATH + "/my-appointments", patientToken);

        assertEquals("Viêm họng cấp", mine.get(0).path("diagnosis").asText());
    }

    @Test
    void analyticsListsAppointmentFeesWithRealStatusNotFakePayments() throws Exception {
        String bookingCode = book().path("bookingCode").asText();

        JsonNode analytics = getJson("/api/v1/admin/analytics?year=" + day.getYear(), adminToken);

        assertFalse(analytics.has("transactions"));
        JsonNode row = null;
        for (JsonNode r : analytics.path("appointmentFees")) {
            if (bookingCode.equals(r.path("bookingCode").asText())) row = r;
        }
        assertTrue(row != null, analytics.path("appointmentFees").toString());
        assertEquals("CONFIRMED", row.path("status").asText());
        assertFalse(row.has("paymentMethod"));
        assertFalse(row.has("transactionId"));
    }

    @Test
    void doctorAppointmentViewsAreAdminOnly() throws Exception {
        String path = "/api/v1/admin/doctors/" + doctorId + "/appointments";
        var response = mvc.perform(get(path).servletPath(path).header("Authorization", "Bearer " + patientToken))
                .andReturn().getResponse();
        assertEquals(403, response.getStatus());
    }
}
