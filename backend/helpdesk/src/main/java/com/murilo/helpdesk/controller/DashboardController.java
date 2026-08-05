package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.response.DashboardStatsResponse;
import com.murilo.helpdesk.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
@Tag(name = "Dashboard", description = "Indicadores do painel, filtrados pelo papel do usuário")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Indicadores do usuário autenticado")
    public ResponseEntity<DashboardStatsResponse> getStats(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getStats(auth.getName()));
    }
}
