package com.murilo.helpdesk.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Escreve o corpo de erro das falhas de autenticação/autorização, que acontecem
 * dentro da cadeia de filtros — antes do Spring MVC e, portanto, fora do alcance
 * do {@code GlobalExceptionHandler}.
 *
 * O JSON é montado à mão de propósito: o Spring Boot 4 usa Jackson 3
 * ({@code tools.jackson.databind}) e não publica um bean do
 * {@code com.fasterxml.jackson.databind.ObjectMapper} do Jackson 2. Como o
 * payload é pequeno e de formato fixo, escrevê-lo diretamente evita depender de
 * qual versão do Jackson está no classpath.
 */
@Component
public class JsonErrorWriter {

    /** Mesmo formato de {@link com.murilo.helpdesk.exception.ApiError}. */
    public void escrever(HttpServletResponse response, HttpStatus status,
                         String mensagem, String path) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String corpo = """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}"""
                .formatted(
                        LocalDateTime.now(),
                        status.value(),
                        escapar(status.getReasonPhrase()),
                        escapar(mensagem),
                        escapar(path));

        response.getWriter().write(corpo);
    }

    /** Escapa o texto para uso dentro de uma string JSON (o path vem da requisição). */
    private String escapar(String valor) {
        if (valor == null) return "";

        StringBuilder sb = new StringBuilder(valor.length() + 16);
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
