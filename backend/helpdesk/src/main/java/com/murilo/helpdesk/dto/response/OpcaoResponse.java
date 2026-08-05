package com.murilo.helpdesk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Item de enum exposto para o front montar selects e badges sem manter
 * cópias dos códigos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpcaoResponse(Integer codigo, String nome, String rotulo, Integer horasSla) {

    public static OpcaoResponse de(Integer codigo, String nome, String rotulo) {
        return new OpcaoResponse(codigo, nome, rotulo, null);
    }
}
