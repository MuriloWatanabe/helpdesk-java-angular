package com.murilo.helpdesk.dto.response;

import java.util.List;


public record DashboardStatsResponse(
        String escopo,

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
