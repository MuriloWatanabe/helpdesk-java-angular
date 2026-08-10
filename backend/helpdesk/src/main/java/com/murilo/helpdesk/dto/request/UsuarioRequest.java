package com.murilo.helpdesk.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioRequest(
        @NotBlank(message = "Informe o nome")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "Informe um e-mail válido")
        String email,


        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
        String cargo,

        Boolean ativo,

        @NotEmpty(message = "Selecione ao menos um perfil")
        Set<Integer> perfis
) {}
