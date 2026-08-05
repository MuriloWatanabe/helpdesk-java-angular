package com.murilo.helpdesk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.murilo.helpdesk.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Requisição sem token (ou com token inválido/expirado) responde 401 em JSON.
 *
 * Antes o Spring devolvia 403 nesse caso, o que impedia o front de distinguir
 * "preciso logar de novo" de "não tenho permissão para esta ação".
 */
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiError body = ApiError.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Sessão expirada ou inexistente. Faça login novamente.",
                request.getRequestURI());

        objectMapper.writeValue(response.getWriter(), body);
    }
}
