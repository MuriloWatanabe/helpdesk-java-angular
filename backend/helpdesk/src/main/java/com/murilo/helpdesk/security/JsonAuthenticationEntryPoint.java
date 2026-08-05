package com.murilo.helpdesk.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private final JsonErrorWriter errorWriter;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        errorWriter.escrever(response, HttpStatus.UNAUTHORIZED,
                "Sessão expirada ou inexistente. Faça login novamente.",
                request.getRequestURI());
    }
}
