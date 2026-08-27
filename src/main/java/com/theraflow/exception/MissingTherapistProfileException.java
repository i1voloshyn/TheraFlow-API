package com.theraflow.exception;

public class MissingTherapistProfileException extends RuntimeException {
    public MissingTherapistProfileException() {
        super("Current account does not have an associated TherapistProfile");
    }
}
