package com.murilo.helpdesk.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonErrorWriter errorWriter;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object motivo = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERRO);
        String mensagem = motivo instanceof String texto
                ? texto
                : "Sessão expirada ou inexistente. Faça login novamente.";

        errorWriter.escrever(response, HttpStatus.UNAUTHORIZED, mensagem, request.getRequestURI());
    }
}
