package com.theraflow.exception;

import com.theraflow.exception.model.ErrorCode;

public class ResourceConflictException extends TheraflowApiException {
    public ResourceConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
