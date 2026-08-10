package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AvaliacaoRequest(
        @NotNull(message = "Selecione uma nota de 1 a 5")
        @Min(value = 1, message = "A nota deve ser de 1 a 5")
        @Max(value = 5, message = "A nota deve ser de 1 a 5")
        Integer nota,

        @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
        String comentario,


        Set<String> aspectos
) {}
