package com.datlichkhambenh.backend.messaging;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailQueueConsumerTests {
    private final JavaMailSender mail = mock(JavaMailSender.class);

    private static EmailNotificationMessage message() {
        return new EmailNotificationMessage("BOOKING_CONFIRMED", "bn@test.vn", "Bệnh nhân A", "BK-42",
                "BS B", "2026-11-12", "09:00", "09:30", "300000");
    }

    @Test
    void sendsConfirmationFromConfiguredSender() {
        new EmailQueueConsumer(mail, "clinic@gmail.com").consume(message());

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(sent.capture());
        assertEquals("clinic@gmail.com", sent.getValue().getFrom());
        assertArrayEquals(new String[]{"bn@test.vn"}, sent.getValue().getTo());
        assertTrue(sent.getValue().getSubject().contains("BK-42"));
        assertTrue(sent.getValue().getText().contains("09:00 - 09:30"));
    }

    @Test
    void leavesSenderToSmtpDefaultWhenNotConfigured() {
        new EmailQueueConsumer(mail, "").consume(message());

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(sent.capture());
        assertNull(sent.getValue().getFrom());
    }

    @Test
    void smtpFailurePropagatesSoRabbitMqCanRetry() {
        doThrow(new org.springframework.mail.MailSendException("smtp down"))
                .when(mail).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        assertThrows(org.springframework.mail.MailSendException.class,
                () -> new EmailQueueConsumer(mail, "").consume(message()));
    }
}
