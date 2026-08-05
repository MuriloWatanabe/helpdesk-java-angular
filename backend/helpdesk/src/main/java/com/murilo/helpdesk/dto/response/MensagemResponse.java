package com.murilo.helpdesk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Resposta simples de confirmação para ações sem corpo de retorno. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MensagemResponse(String mensagem, String detalhe) {

    public static MensagemResponse de(String mensagem) {
        return new MensagemResponse(mensagem, null);
    }
}
