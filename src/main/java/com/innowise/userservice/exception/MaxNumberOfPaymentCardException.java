package com.innowise.userservice.exception;

public class MaxNumberOfPaymentCardException extends RuntimeException {
    public MaxNumberOfPaymentCardException(String message) {
        super(message);
    }
}