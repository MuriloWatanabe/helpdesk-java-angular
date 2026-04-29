package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        String titulo,
        String observacoes,
        Integer status,
        Integer prioridade,
        UsuarioResumoResponse tecnico,
        UsuarioResumoResponse cliente,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        LocalDateTime dataAtualizacao
) {}
