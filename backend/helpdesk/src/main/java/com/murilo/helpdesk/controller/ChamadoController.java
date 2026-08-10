package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.request.AtribuirTecnicoRequest;
import com.murilo.helpdesk.dto.request.ChamadoFiltro;
import com.murilo.helpdesk.dto.request.ChamadoRequest;
import com.murilo.helpdesk.dto.response.ChamadoResponse;
import com.murilo.helpdesk.dto.response.HistoricoResponse;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.service.ChamadoService;
import com.murilo.helpdesk.service.HistoricoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/chamados")
@Tag(name = "Chamados", description = "Abertura, acompanhamento e atendimento de chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final HistoricoService historicoService;


    @GetMapping
    @Operation(summary = "Listar chamados com filtros e paginação")
    public ResponseEntity<Page<ChamadoResponse>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer prioridade,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Boolean semTecnico,
            @RequestParam(required = false) Boolean slaVencido,
            @RequestParam(required = false) Boolean apenasPendentes,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @PageableDefault(size = 10, sort = "dataAbertura",
                    direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable,
            Authentication auth) {

        var filtro = new ChamadoFiltro(q, status, prioridade, categoria, tecnicoId, clienteId,
                semTecnico, slaVencido, apenasPendentes, dataInicio, dataFim);

        return ResponseEntity.ok(chamadoService.listar(filtro, pageable, auth.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar chamado por ID")
    public ResponseEntity<ChamadoResponse> buscarPorId(@PathVariable Long id,
                                                       Authentication auth) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id, auth.getName()));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Linha do tempo do chamado")
    public ResponseEntity<List<HistoricoResponse>> historico(@PathVariable Long id,
                                                             Authentication auth) {

        chamadoService.buscarPorId(id, auth.getName());
        return ResponseEntity.ok(historicoService.listar(id));
    }

    @PostMapping
    @Operation(summary = "Abrir novo chamado")
    public ResponseEntity<ChamadoResponse> criar(@Valid @RequestBody ChamadoRequest request,
                                                 Authentication auth) {
        ChamadoResponse criado = chamadoService.criar(request, auth.getName());
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do chamado")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ChamadoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ChamadoRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(chamadoService.atualizar(id, request, auth.getName()));
    }


    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Alterar status do chamado")
    public ResponseEntity<ChamadoResponse> alterarStatus(@PathVariable Long id,
                                                         @PathVariable Status status,
                                                         Authentication auth) {
        return ResponseEntity.ok(chamadoService.alterarStatus(id, status, auth.getName()));
    }

    @PatchMapping("/{id}/assumir")
    @Operation(summary = "Assumir o chamado (técnico)")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ChamadoResponse> assumir(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(chamadoService.assumir(id, auth.getName()));
    }

    @PatchMapping("/{id}/tecnico")
    @Operation(summary = "Atribuir ou remover o técnico responsável")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ChamadoResponse> atribuirTecnico(
            @PathVariable Long id,
            @RequestBody AtribuirTecnicoRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                chamadoService.atribuirTecnico(id, request.tecnicoId(), auth.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir chamado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication auth) {
        chamadoService.excluir(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
