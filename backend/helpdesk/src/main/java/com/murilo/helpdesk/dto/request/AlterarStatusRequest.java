package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.Size;


public record AlterarStatusRequest(
        @Size(max = 2000, message = "A observação deve ter no máximo 2000 caracteres")
        String comentario
) {}
