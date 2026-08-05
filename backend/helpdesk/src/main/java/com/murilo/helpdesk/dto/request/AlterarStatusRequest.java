package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Corpo opcional ao mudar o status: permite registrar a justificativa junto
 * com a mudança, que vira um comentário no chamado.
 */
public record AlterarStatusRequest(
        @Size(max = 2000, message = "A observação deve ter no máximo 2000 caracteres")
        String comentario
) {}
