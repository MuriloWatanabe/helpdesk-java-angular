package com.murilo.helpdesk.dto;

import java.util.Set;

public record LoginResponse(
        String token,
        String tipo,
        String email,
        Set<String> perfis
) {}
