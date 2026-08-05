package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record AvaliacaoResponse(
        Long id,
        Long chamadoId,
        Integer nota,
        String interpretacao,
        String comentario,
        Set<String> aspectos,
        UsuarioResumoResponse avaliadoPor,
        LocalDateTime dataAvaliacao
) {}
