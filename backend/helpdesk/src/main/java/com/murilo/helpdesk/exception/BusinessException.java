package com.murilo.helpdesk.exception;

/**
 * Regra de negócio violada — mapeado para HTTP 400 (ou 409 quando é conflito
 * de estado, como e-mail duplicado).
 */
public class BusinessException extends RuntimeException {

    private final boolean conflito;

    public BusinessException(String message) {
        this(message, false);
    }

    public BusinessException(String message, boolean conflito) {
        super(message);
        this.conflito = conflito;
    }

    /** Fábrica para conflitos (HTTP 409): recurso duplicado, estado incompatível. */
    public static BusinessException conflito(String message) {
        return new BusinessException(message, true);
    }

    public boolean isConflito() {
        return conflito;
    }
}
