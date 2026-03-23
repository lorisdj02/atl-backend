package com.atl.backend.exception;

import lombok.experimental.StandardException;

@StandardException
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
    
}
