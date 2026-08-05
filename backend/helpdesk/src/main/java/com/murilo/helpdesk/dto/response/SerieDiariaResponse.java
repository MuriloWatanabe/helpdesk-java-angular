package com.murilo.helpdesk.dto.response;

import java.time.LocalDate;

/** Ponto da série temporal de aberturas (gráfico de evolução). */
public record SerieDiariaResponse(LocalDate data, long total) {}
