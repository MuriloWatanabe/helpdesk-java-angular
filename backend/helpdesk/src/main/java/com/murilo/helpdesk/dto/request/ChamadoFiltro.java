package com.murilo.helpdesk.dto.request;

import java.time.LocalDate;


public record ChamadoFiltro(
        String q,
        Integer status,
        Integer prioridade,
        Integer categoria,
        Long tecnicoId,
        Long clienteId,
        Boolean semTecnico,
        Boolean slaVencido,
        Boolean apenasPendentes,
        LocalDate dataInicio,
        LocalDate dataFim
) {

    public boolean temBusca() {
        return q != null && !q.isBlank();
    }
}
