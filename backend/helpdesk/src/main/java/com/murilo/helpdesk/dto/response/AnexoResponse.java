package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;

public record AnexoResponse(
        Long id,
        Long chamadoId,
        String nomeArquivo,
        String tipoMime,
        Long tamanho,
        String tamanhoFormatado,
        boolean publico,
        boolean imagem,
        UsuarioResumoResponse enviadoPor,
        LocalDateTime dataUpload
) {}
