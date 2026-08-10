package com.murilo.helpdesk.exception;


public class BusinessException extends RuntimeException {

    private final boolean conflito;

    public BusinessException(String message) {
        this(message, false);
    }

    public BusinessException(String message, boolean conflito) {
        super(message);
        this.conflito = conflito;
    }


    public static BusinessException conflito(String message) {
        return new BusinessException(message, true);
    }

    public boolean isConflito() {
        return conflito;
    }
}
