package com.datlichkhambenh.backend.facade;

import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Facade tickets 23-24: the five booking endpoints behave exactly as before, now behind BookingFacade. */
class BookingFacadeIntegrationTests extends BookingApiFixture {

    @Test
    void confirmBookingCreatesAStandardAppointment() throws Exception {
        var appointment = book();

        assertEquals("CONFIRMED", appointment.path("status").asText());
        assertEquals("STANDARD", appointment.path("pricingKey").asText());
        assertEquals(scheduleId.longValue(), appointment.path("scheduleId").asLong());
    }

    @Test
    void legacyCreateStillBooksWithoutAQuote() throws Exception {
        mvc.perform(post(PATH).servletPath(PATH).header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("scheduleId", scheduleId, "symptoms", "cũ"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Đặt lịch khám thành công"))
                .andExpect(jsonPath("$.data.pricingKey").value("STANDARD"));
    }

    @Test
    void confirmRouteStillDemandsTheQuote() throws Exception {
        var body = confirmBody();
        body.remove("expectedPrice");

        assertEquals(400, confirm(body).getStatus());
    }

    @Test
    void patientCancelsAndSeesTheResultInTheirList() throws Exception {
        long id = book().path("id").asLong();

        cancel(id);

        String path = PATH + "/my-appointments";
        mvc.perform(get(path).servletPath(path).header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(id))
                .andExpect(jsonPath("$.data[0].status").value("CANCELLED"));
    }

    @Test
    void detailsAreVisibleToTheOwnerAndTheDoctorButNotToAnotherPatient() throws Exception {
        long id = book().path("id").asLong();
        String path = PATH + "/" + id;

        mvc.perform(get(path).servletPath(path).header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));

        String doctorEmail = users.findById(doctorId).orElseThrow().getEmail();
        mvc.perform(get(path).servletPath(path).header("Authorization", "Bearer " + jwt.generateToken(doctorEmail, "DOCTOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));

        String stranger = new TransactionTemplate(tm).execute(tx -> users.save(User.builder()
                .email("stranger-" + UUID.randomUUID() + "@test.vn").passwordHash("unused")
                .role("PATIENT").isActive(true).build()).getEmail());
        mvc.perform(get(path).servletPath(path).header("Authorization", "Bearer " + jwt.generateToken(stranger, "PATIENT")))
                .andExpect(status().is4xxClientError());
    }
}
