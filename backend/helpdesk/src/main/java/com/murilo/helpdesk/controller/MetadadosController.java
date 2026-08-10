package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.response.OpcaoResponse;
import com.murilo.helpdesk.model.enums.Categoria;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/v1/metadados")
@Tag(name = "Metadados", description = "Listas de domínio (status, prioridades, categorias)")
public class MetadadosController {

    @GetMapping
    @Operation(summary = "Todas as listas de domínio")
    public ResponseEntity<Map<String, List<OpcaoResponse>>> todos() {
        return ResponseEntity.ok(Map.of(
                "status", status(),
                "prioridades", prioridades(),
                "categorias", categorias(),
                "perfis", perfis()));
    }

    @GetMapping("/status")
    @Operation(summary = "Status possíveis do chamado")
    public List<OpcaoResponse> status() {
        return Arrays.stream(Status.values())
                .map(s -> OpcaoResponse.de(s.getCodigo(), s.name(), s.getDescricao()))
                .toList();
    }

    @GetMapping("/prioridades")
    @Operation(summary = "Prioridades e respectivos prazos de SLA")
    public List<OpcaoResponse> prioridades() {
        return Arrays.stream(Prioridade.values())
                .map(p -> new OpcaoResponse(p.getCodigo(), p.name(), p.getDescricao(),
                        p.getHorasSla()))
                .toList();
    }

    @GetMapping("/categorias")
    @Operation(summary = "Categorias de problema")
    public List<OpcaoResponse> categorias() {
        return Arrays.stream(Categoria.values())
                .map(c -> OpcaoResponse.de(c.getCodigo(), c.name(), c.getDescricao()))
                .toList();
    }

    @GetMapping("/perfis")
    @Operation(summary = "Perfis de acesso")
    public List<OpcaoResponse> perfis() {
        return Arrays.stream(Perfil.values())
                .map(p -> OpcaoResponse.de(p.getCodigo(), p.getDescricao(), p.getRotulo()))
                .toList();
    }
}
