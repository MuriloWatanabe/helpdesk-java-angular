package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "Escreva algo antes de enviar")
        @Size(min = 2, max = 2000, message = "O comentário deve ter entre 2 e 2000 caracteres")
        String texto,

        /** Nota interna: só técnicos e administradores enxergam. */
        Boolean interno
) {}
