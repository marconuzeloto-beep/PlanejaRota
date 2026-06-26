package com.logicore.shared.domain.exception;

import java.util.UUID;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String entity, UUID id) {
        super(entity + " não encontrado: " + id);
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
