package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank(message = "Informe um título para o chamado")
        @Size(min = 5, max = 200, message = "O título deve ter entre 5 e 200 caracteres")
        String titulo,

        @NotBlank(message = "Descreva o problema")
        @Size(min = 10, max = 2000, message = "A descrição deve ter entre 10 e 2000 caracteres")
        String observacoes,

        @NotNull(message = "Selecione a prioridade")
        Integer prioridade,

        @NotNull(message = "Selecione a categoria")
        Integer categoria,

        Long tecnicoId,
        Long clienteId
) {}
