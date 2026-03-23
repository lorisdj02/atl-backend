package com.atl.backend.exception;

import lombok.experimental.StandardException;

@StandardException
public class PaymentRequiredException extends RuntimeException {
    public PaymentRequiredException(String message) {
        super(message);
    }
    
}
