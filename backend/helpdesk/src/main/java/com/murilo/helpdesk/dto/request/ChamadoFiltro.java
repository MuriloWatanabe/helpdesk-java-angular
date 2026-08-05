package com.murilo.helpdesk.dto.request;

import java.time.LocalDate;

/**
 * Filtros da listagem de chamados. Todos opcionais — os nulos são ignorados.
 *
 * Antes a busca e o filtro de status eram aplicados no navegador sobre a página
 * já carregada, então "Encerrados" só encontrava o que estivesse nos 10
 * primeiros registros. Agora a consulta acontece no banco.
 */
public record ChamadoFiltro(
        String q,                 // busca em número, título, descrição e nome do cliente
        Integer status,
        Integer prioridade,
        Integer categoria,
        Long tecnicoId,
        Long clienteId,
        Boolean semTecnico,       // true = somente os que aguardam atribuição
        Boolean slaVencido,       // true = somente os que estouraram o prazo
        Boolean apenasPendentes,  // true = exclui encerrados e cancelados
        LocalDate dataInicio,
        LocalDate dataFim
) {

    public boolean temBusca() {
        return q != null && !q.isBlank();
    }
}
