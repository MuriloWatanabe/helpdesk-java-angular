package com.murilo.helpdesk.dto.response;

import java.time.LocalDate;


public record SerieDiariaResponse(LocalDate data, long total) {}
