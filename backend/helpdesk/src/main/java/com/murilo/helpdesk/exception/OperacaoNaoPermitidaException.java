package com.murilo.helpdesk.exception;

/**
 * Usuário autenticado tentando acessar/alterar algo que não lhe pertence —
 * mapeado para HTTP 403.
 */
public class OperacaoNaoPermitidaException extends RuntimeException {

    public OperacaoNaoPermitidaException(String message) {
        super(message);
    }
}
