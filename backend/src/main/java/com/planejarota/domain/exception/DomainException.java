package com.planejarota.domain.exception;

/**
 * Raiz da hierarquia de exceções de domínio.
 * Unchecked para não poluir assinaturas de métodos com checked exceptions.
 * Cada exceção concreta documenta uma violação de regra de negócio específica.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
