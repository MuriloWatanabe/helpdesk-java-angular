package com.murilo.helpdesk.dto.response;

import java.util.List;

/**
 * Estatísticas já filtradas pelo papel de quem pediu: o cliente recebe apenas
 * números dos próprios chamados, o técnico dos que atende e o admin de todos.
 */
public record DashboardStatsResponse(
        String escopo,              // "GLOBAL" | "TECNICO" | "CLIENTE"

        long totalChamados,
        long totalAbertos,
        long totalEmAndamento,
        long totalAguardandoCliente,
        long totalResolvidos,
        long totalEncerrados,
        long totalCancelados,

        long totalSlaVencido,
        long totalSlaEmRisco,
        long totalSemTecnico,

        Double tempoMedioResolucaoHoras,
        Double notaMediaAtendimento,
        long totalAvaliacoes,

        List<ContagemResponse> porPrioridade,
        List<ContagemResponse> porCategoria,
        List<ContagemResponse> porTecnico,
        List<SerieDiariaResponse> aberturasPorDia,

        List<ChamadoResponse> chamadosRecentes
) {}
