package com.theraflow.exception;

import com.theraflow.exception.model.ErrorCode;
import lombok.Getter;

@Getter
public class TheraflowApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public TheraflowApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TheraflowApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
