package com.datlichkhambenh.backend.exception;

import org.springframework.http.HttpStatus;

public class BookingConflictException extends AppException {

    public BookingConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
