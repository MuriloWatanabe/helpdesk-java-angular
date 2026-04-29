package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres")
        String titulo,

        @NotBlank(message = "Observações são obrigatórias")
        @Size(min = 10, max = 2000, message = "Observações devem ter entre 10 e 2000 caracteres")
        String observacoes,

        @NotNull(message = "Prioridade é obrigatória")
        Integer prioridade,

        Long tecnicoId,
        Long clienteId
) {}
