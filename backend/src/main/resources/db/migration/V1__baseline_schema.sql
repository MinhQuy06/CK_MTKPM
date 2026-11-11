-- V1: schema gốc (sinh từ entity, gồm cả các bản nâng cấp db/upgrade/**).
SET FOREIGN_KEY_CHECKS=0;
CREATE TABLE `appointments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_price` decimal(12,2) DEFAULT NULL,
  `benefit_request_id` bigint DEFAULT NULL,
  `booking_code` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `discount_rate` decimal(19,18) DEFAULT NULL,
  `price` decimal(12,2) DEFAULT NULL,
  `pricing_key` varchar(20) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `symptoms` text,
  `doctor_id` bigint NOT NULL,
  `patient_id` bigint NOT NULL,
  `schedule_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appointments_booking_code` (`booking_code`),
  UNIQUE KEY `uk_appointments_schedule` (`schedule_id`),
  KEY `fk_appointments_doctor` (`doctor_id`),
  KEY `fk_appointments_patient` (`patient_id`),
  CONSTRAINT `fk_appointments_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`user_id`),
  CONSTRAINT `fk_appointments_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`user_id`),
  CONSTRAINT `fk_appointments_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `benefit_decisions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(20) NOT NULL,
  `decided_at` datetime(6) NOT NULL,
  `decided_by` bigint NOT NULL,
  `reason` varchar(1000) NOT NULL,
  `request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK79gaa8xh1xfqmwqogyvm1lisf` (`request_id`),
  CONSTRAINT `FK79gaa8xh1xfqmwqogyvm1lisf` FOREIGN KEY (`request_id`) REFERENCES `benefit_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `benefit_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `approved_from` date DEFAULT NULL,
  `approved_until` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `kind` varchar(20) NOT NULL,
  `reference_code` varchar(120) NOT NULL,
  `renewal_of_id` bigint DEFAULT NULL,
  `requested_from` date NOT NULL,
  `requested_until` date NOT NULL,
  `status` varchar(20) NOT NULL,
  `version` bigint NOT NULL,
  `patient_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_benefit_patient` (`patient_id`,`created_at`),
  CONSTRAINT `FKibhq2jbdd4dm44a1qtdvj65fm` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `doctors` (
  `user_id` bigint NOT NULL,
  `consultation_fee` decimal(12,2) DEFAULT NULL,
  `degree` varchar(100) DEFAULT NULL,
  `experience_years` int DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `is_approved` bit(1) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `specialty_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_doctors_phone` (`phone`),
  KEY `fk_doctors_specialty` (`specialty_id`),
  CONSTRAINT `fk_doctors_specialty` FOREIGN KEY (`specialty_id`) REFERENCES `specialties` (`id`),
  CONSTRAINT `fk_doctors_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `medical_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `diagnosis` text,
  `doctor_notes` text,
  `appointment_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medical_records_appointment` (`appointment_id`),
  CONSTRAINT `fk_medical_records_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `medical_services` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `code` varchar(30) NOT NULL,
  `default_price` decimal(12,2) NOT NULL,
  `description` text,
  `duration_minutes` int NOT NULL,
  `name` varchar(150) NOT NULL,
  `specialty_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4136oe6illqkyi5j4a87q5bhc` (`code`),
  KEY `FKst689qoxk61bd6dn5ofnji0uv` (`specialty_id`),
  CONSTRAINT `FKst689qoxk61bd6dn5ofnji0uv` FOREIGN KEY (`specialty_id`) REFERENCES `specialties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `notification_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_code` varchar(50) NOT NULL,
  `channel` varchar(10) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `type` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_log_booking` (`booking_code`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `patients` (
  `user_id` bigint NOT NULL,
  `address` text,
  `dob` date DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `medical_history` text,
  `phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_patients_phone` (`phone`),
  CONSTRAINT `fk_patients_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `schedules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` time(6) NOT NULL,
  `is_available` bit(1) NOT NULL,
  `start_time` time(6) NOT NULL,
  `work_date` date NOT NULL,
  `doctor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_schedules_doctor` (`doctor_id`),
  CONSTRAINT `fk_schedules_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `specialties` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
SET FOREIGN_KEY_CHECKS=1;
