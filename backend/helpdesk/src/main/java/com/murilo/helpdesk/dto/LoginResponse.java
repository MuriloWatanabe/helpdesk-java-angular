package com.murilo.helpdesk.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record LoginResponse(
        Long id,
        String token,
        String tipo,
        String nome,
        String email,
        Set<String> perfis,
        /** Momento em que o token deixa de valer — o front usa para deslogar antes da falha. */
        LocalDateTime expiraEm
) {}
