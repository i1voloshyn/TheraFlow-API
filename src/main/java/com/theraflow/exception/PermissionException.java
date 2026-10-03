package com.theraflow.exception;

import com.theraflow.exception.model.ErrorCode;

public class PermissionException extends TheraflowApiException {

    public PermissionException(ErrorCode errorCode) {
        super(errorCode);
    }
}
