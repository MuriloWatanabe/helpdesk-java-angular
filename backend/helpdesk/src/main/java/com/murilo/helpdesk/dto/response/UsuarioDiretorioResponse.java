package com.murilo.helpdesk.dto.response;

import java.util.Set;

/** Dados mínimos necessários para seleção de pessoas nos fluxos de chamados. */
public record UsuarioDiretorioResponse(
        Long id,
        String nome,
        String email,
        Set<String> perfis
) {}
