package com.theraflow.exception;

import com.theraflow.exception.model.ErrorCode;

import java.util.UUID;

public class EntityNotFoundException extends TheraflowApiException {

    public EntityNotFoundException(ErrorCode errorCode, UUID id) {
        super(errorCode, "%s: %s".formatted(errorCode.getTitle(), id));
    }

    public EntityNotFoundException(ErrorCode errorCode, String identifier) {
        super(errorCode, "%s: %s".formatted(errorCode.getTitle(), identifier));
    }

}
