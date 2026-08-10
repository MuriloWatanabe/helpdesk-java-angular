package com.murilo.helpdesk.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<CampoInvalido> campos
) {

    public record CampoInvalido(String campo, String mensagem) {}

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ApiError of(int status, String error, String message, String path,
                              List<CampoInvalido> campos) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, campos);
    }
}
