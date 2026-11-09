package com.datlichkhambenh.backend.exception;
import org.springframework.http.HttpStatus;
public class BookingRejectionException extends AppException {
 private final String code;
 public BookingRejectionException(String code,String message,HttpStatus status){super(message,status);this.code=code;}
 public String getCode(){return code;}
}
