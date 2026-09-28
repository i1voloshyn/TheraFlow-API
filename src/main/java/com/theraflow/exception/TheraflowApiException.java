package com.theraflow.exception;

import com.theraflow.exception.model.ErrorCode;
import lombok.Getter;

@Getter
public class TheraflowApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public TheraflowApiException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public TheraflowApiException(ErrorCode errorCode, Throwable cause) {
        this.errorCode = errorCode;
    }

}
