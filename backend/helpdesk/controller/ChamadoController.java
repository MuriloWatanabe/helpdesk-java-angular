package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.service.ChamadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@Slf4j
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
        log.info("Buscando chamado por ID: {}", id);
        Chamado chamado = chamadoService.findById(id);
        return ResponseEntity.ok(chamado);
    }

    /**
     * Lista todos os chamados do sistema com paginação.
     * @param pageable Parâmetros de paginação
     * @return Página de chamados
     */
    @GetMapping
    @Operation(summary = "Listar todos os chamados", description = "Retorna uma página com todos os chamados cadastrados")
    public ResponseEntity<Page<Chamado>> findAll(@PageableDefault(size = 10) Pageable pageable) {
        log.info("Buscando todos os chamados com paginação: {}", pageable);
        Page<Chamado> chamados = chamadoService.findAll(pageable);
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
        log.info("Criando novo chamado: {}", chamado.getTitulo());
        Chamado novoChamado = chamadoService.create(chamado);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novoChamado.getId())
                .toUri();
        log.info("Chamado criado com ID: {}", novoChamado.getId());
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
        log.info("Atualizando status do chamado ID {} para {}", id, status);
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
        log.info("Atualizando chamado ID: {}", id);
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
        log.info("Deletando chamado ID: {}", id);
        chamadoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Trata exceções de entidade não encontrada.
     * @param ex Exceção lançada
     * @return Resposta com erro 404
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        log.error("Entidade não encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Trata exceções de validação.
     * @param ex Exceção lançada
     * @return Resposta com erro 400
     */
    @ExceptionHandler(jakarta.validation.ValidationException.class)
    public ResponseEntity<String> handleValidationException(jakarta.validation.ValidationException ex) {
        log.error("Erro de validação: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Trata exceções gerais.
     * @param ex Exceção lançada
     * @return Resposta com erro 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("Erro interno: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
    }
}
