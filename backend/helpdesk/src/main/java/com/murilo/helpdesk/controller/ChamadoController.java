package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.service.ChamadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/v1/chamados")
@Tag(name = "Chamados", description = "Gerenciamento de chamados técnicos")
@RequiredArgsConstructor
@Slf4j
public class ChamadoController {

    private final ChamadoService chamadoService;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar chamado por ID")
    public ResponseEntity<Chamado> findById(
            @Parameter(description = "ID do chamado") @PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar chamados com paginação")
    public ResponseEntity<Page<Chamado>> findAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(chamadoService.findAll(pageable));
    }

    @PostMapping
    @Operation(summary = "Abrir novo chamado")
    public ResponseEntity<Chamado> create(@Valid @RequestBody Chamado chamado) {
        Chamado criado = chamadoService.create(chamado);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar chamado")
    public ResponseEntity<Chamado> update(
            @Parameter(description = "ID do chamado") @PathVariable Long id,
            @Valid @RequestBody Chamado chamado) {
        return ResponseEntity.ok(chamadoService.update(id, chamado));
    }

    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Atualizar status do chamado")
    public ResponseEntity<Chamado> updateStatus(
            @Parameter(description = "ID do chamado") @PathVariable Long id,
            @Parameter(description = "Novo status") @PathVariable Status status) {
        return ResponseEntity.ok(chamadoService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar chamado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do chamado") @PathVariable Long id) {
        chamadoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
