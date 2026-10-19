package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "Token de redefinição ausente")
        String token,

        @NotBlank(message = "Informe a nova senha")
        @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres")
        String novaSenha
) {}
