package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Set<String> perfis,
        LocalDateTime dataCriacao
) {}
