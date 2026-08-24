package com.theraflow.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, UUID id) {
        super(String.format("%s with ID %s not found.", entity, id));
    }

}
