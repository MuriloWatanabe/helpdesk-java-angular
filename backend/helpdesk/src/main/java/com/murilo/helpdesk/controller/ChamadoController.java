package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.request.ChamadoRequest;
import com.murilo.helpdesk.dto.response.ChamadoResponse;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.service.ChamadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/chamados")
@Tag(name = "Chamados", description = "Gerenciamento de chamados técnicos")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar chamado por ID")
    public ResponseEntity<ChamadoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.findByIdAsResponse(id));
    }

    /**
     * ADMIN/TECNICO → todos os chamados
     * CLIENTE → somente os seus
     */
    @GetMapping
    @Operation(summary = "Listar chamados (filtrado por papel)")
    public ResponseEntity<Page<ChamadoResponse>> findAll(
            @PageableDefault(size = 10) Pageable pageable,
            Authentication auth) {
        return ResponseEntity.ok(chamadoService.findAll(pageable, auth.getName()));
    }

    @PostMapping
    @Operation(summary = "Abrir novo chamado")
    public ResponseEntity<ChamadoResponse> create(
            @Valid @RequestBody ChamadoRequest request,
            Authentication auth) {
        ChamadoResponse criado = chamadoService.create(request, auth.getName());
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar chamado")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ChamadoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoRequest request) {
        return ResponseEntity.ok(chamadoService.update(id, request));
    }

    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Atualizar status do chamado")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ChamadoResponse> updateStatus(
            @PathVariable Long id,
            @PathVariable Status status) {
        return ResponseEntity.ok(chamadoService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar chamado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chamadoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
