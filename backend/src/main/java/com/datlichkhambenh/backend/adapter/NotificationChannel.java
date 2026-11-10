package com.datlichkhambenh.backend.adapter;

import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;

/**
 * Target (GoF Adapter): the one way the clinic sends an appointment notification over an extra
 * channel. Callers never see which vendor sits behind it.
 */
public interface NotificationChannel {

    /** Channel name as written to the notification history, e.g. "SMS". */
    String name();

    /**
     * @param patientPhone the patient's phone as stored by the clinic (may be null or local format)
     * @param message      what to tell the patient
     */
    DeliveryResult send(String patientPhone, EmailNotificationMessage message);
}
