package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.service.ChamadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

/**
 * Controller responsável pelas operações de chamados técnicos.
 * Fornece endpoints para gerenciamento completo do ciclo de vida dos chamados.
 */
@RestController
@RequestMapping("/api/v1/chamados")
@Tag(name = "Chamados", description = "API de gerenciamento de chamados técnicos")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    /**
     * Busca um chamado pelo ID.
     * @param id ID do chamado
     * @return Chamado encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar chamado por ID", description = "Retorna os detalhes de um chamado específico")
    public ResponseEntity<Chamado> findById(
            @Parameter(description = "ID do chamado") @PathVariable Long id) {
        Chamado chamado = chamadoService.findById(id);
        return ResponseEntity.ok(chamado);
    }

    /**
     * Lista todos os chamados do sistema.
     * @return Lista de chamados
     */
    @GetMapping
    @Operation(summary = "Listar todos os chamados", description = "Retorna uma lista com todos os chamados cadastrados")
    public ResponseEntity<List<Chamado>> findAll() {
        List<Chamado> chamados = chamadoService.findAll();
        return ResponseEntity.ok(chamados);
    }

    /**
     * Cria um novo chamado no sistema.
     * @param chamado Dados do novo chamado
     * @return Chamado criado com status 201
     */
    @PostMapping
    @Operation(summary = "Criar novo chamado", description = "Abre um novo chamado técnico no sistema")
    public ResponseEntity<Chamado> create(@Valid @RequestBody Chamado chamado) {
        Chamado novoChamado = chamadoService.create(chamado);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novoChamado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(novoChamado);
    }

    /**
     * Atualiza o status de um chamado.
     * @param id ID do chamado
     * @param status Novo status
     * @return Chamado com status atualizado
     */
    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Atualizar status do chamado", description = "Altera o status de um chamado existente")
    public ResponseEntity<Chamado> updateStatus(
            @Parameter(description = "ID do chamado") @PathVariable Long id,
            @Parameter(description = "Novo status") @PathVariable Status status) {
        Chamado chamadoAtualizado = chamadoService.updateStatus(id, status);
        return ResponseEntity.ok(chamadoAtualizado);
    }

    /**
     * Atualiza os dados de um chamado.
     * @param id ID do chamado
     * @param chamado Dados atualizados
     * @return Chamado atualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar chamado", description = "Atualiza os dados de um chamado existente")
    public ResponseEntity<Chamado> update(
            @Parameter(description = "ID do chamado") @PathVariable Long id,
            @Valid @RequestBody Chamado chamado) {
        Chamado chamadoAtualizado = chamadoService.update(id, chamado);
        return ResponseEntity.ok(chamadoAtualizado);
    }

    /**
     * Deleta um chamado do sistema.
     * @param id ID do chamado
     * @return Status 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar chamado", description = "Remove um chamado do sistema")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do chamado") @PathVariable Long id) {
        chamadoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
