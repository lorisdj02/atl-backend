package com.atl.backend.exception;

import lombok.experimental.StandardException;

@StandardException
public class UnauthorizedException  extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
    
}
