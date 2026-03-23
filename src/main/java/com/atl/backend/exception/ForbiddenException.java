package com.atl.backend.exception;

import lombok.experimental.StandardException;

@StandardException
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
