package com.murilo.helpdesk.dto.response;

import java.util.List;

public record DashboardStatsResponse(
        long totalAbertos,
        long totalEmAndamento,
        long totalEncerrados,
        long totalChamados,
        List<ChamadoResponse> chamadosRecentes
) {}
