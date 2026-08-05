package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;

public record HistoricoResponse(
        Long id,
        String tipo,
        String descricao,
        String valorAnterior,
        String valorNovo,
        UsuarioResumoResponse usuario,
        LocalDateTime dataAlteracao
) {}
