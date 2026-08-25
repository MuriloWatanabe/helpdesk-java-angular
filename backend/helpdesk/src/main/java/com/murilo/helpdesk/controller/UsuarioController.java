package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.request.UsuarioRequest;
import com.murilo.helpdesk.dto.response.UsuarioResponse;
import com.murilo.helpdesk.dto.response.UsuarioDiretorioResponse;
import com.murilo.helpdesk.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/v1/usuarios")
@Tag(name = "Usuários", description = "Gestão de usuários e perfis de acesso")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;


    @GetMapping
    @Operation(summary = "Listar usuários com filtros opcionais")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) Integer perfil,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(usuarioService.listar(perfil, ativo, q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findByIdAsResponse(id));
    }

    @GetMapping("/diretorio")
    @Operation(summary = "Listar diretório operacional ativo")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<List<UsuarioDiretorioResponse>> listarDiretorio() {
        return ResponseEntity.ok(usuarioService.listarDiretorioAtivo());
    }

    @PostMapping
    @Operation(summary = "Criar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse criado = usuarioService.create(request);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @PatchMapping("/{id}/perfis")
    @Operation(summary = "Atualizar perfis de acesso")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> atualizarPerfis(@PathVariable Long id,
                                                           @RequestBody Set<Integer> perfis) {
        return ResponseEntity.ok(usuarioService.updatePerfis(id, perfis));
    }


    @PatchMapping("/{id}/situacao")
    @Operation(summary = "Ativar ou desativar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> alterarSituacao(@PathVariable Long id,
                                                           @RequestParam boolean ativo,
                                                           Authentication auth) {
        return ResponseEntity.ok(usuarioService.alterarSituacao(id, ativo, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário sem chamados vinculados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication auth) {
        usuarioService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
