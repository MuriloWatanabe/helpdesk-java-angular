package com.murilo.helpdesk.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String cargo,
        boolean ativo,
        Set<String> perfis,
        Set<Integer> perfisCodigos,
        String perfilPrincipal,
        LocalDateTime dataCriacao,
        LocalDateTime ultimoAcesso
) {}
