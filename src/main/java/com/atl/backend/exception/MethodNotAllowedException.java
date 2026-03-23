package com.atl.backend.exception;

import lombok.experimental.StandardException;

@StandardException

public class MethodNotAllowedException extends RuntimeException {
    public MethodNotAllowedException(String message) {
        super(message);
    }

}
