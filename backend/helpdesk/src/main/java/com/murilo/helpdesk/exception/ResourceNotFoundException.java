package com.murilo.helpdesk.exception;

/** Recurso inexistente — mapeado para HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String recurso, Object id) {
        return new ResourceNotFoundException(recurso + " não encontrado(a). ID: " + id);
    }
}
