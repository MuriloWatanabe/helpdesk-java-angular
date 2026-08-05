package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;

public record ComentarioResponse(
        Long id,
        Long chamadoId,
        String texto,
        boolean interno,
        boolean editado,
        UsuarioResumoResponse autor,
        String autorPerfil,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
