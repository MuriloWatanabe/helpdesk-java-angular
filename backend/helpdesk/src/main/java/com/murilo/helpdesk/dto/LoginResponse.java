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

        LocalDateTime expiraEm
) {}
