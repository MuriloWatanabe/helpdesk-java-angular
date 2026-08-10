package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record AtualizarPerfilRequest(
        @NotBlank(message = "Informe o nome")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "Informe um e-mail válido")
        String email,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
        String cargo
) {}
