package com.theraflow.exception;

import com.theraflow.exception.model.ErrorCode;

public class InvalidCredentialsException extends TheraflowApiException {

    public InvalidCredentialsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
