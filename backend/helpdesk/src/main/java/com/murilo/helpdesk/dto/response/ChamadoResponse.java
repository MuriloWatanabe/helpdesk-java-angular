package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;

/**
 * Os rótulos (statusLabel, prioridadeLabel, categoriaLabel) vão prontos do
 * servidor para que a tela não precise duplicar os mapas de código → texto.
 */
public record ChamadoResponse(
        Long id,
        String numero,
        String titulo,
        String observacoes,

        Integer status,
        String statusLabel,
        Integer prioridade,
        String prioridadeLabel,
        Integer categoria,
        String categoriaLabel,

        UsuarioResumoResponse tecnico,
        UsuarioResumoResponse cliente,

        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        LocalDateTime dataAtualizacao,
        LocalDateTime dataPrimeiraResposta,

        LocalDateTime prazoSla,
        boolean slaVencido,
        Long horasRestantesSla,

        boolean encerrado,
        boolean avaliado,
        long totalComentarios,
        long totalAnexos
) {}
