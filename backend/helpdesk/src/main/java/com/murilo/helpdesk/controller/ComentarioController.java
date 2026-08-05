package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.request.ComentarioRequest;
import com.murilo.helpdesk.dto.response.ComentarioResponse;
import com.murilo.helpdesk.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/chamados/{chamadoId}/comentarios")
@Tag(name = "Comentários", description = "Conversa entre cliente e equipe de suporte")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping
    @Operation(summary = "Listar comentários do chamado")
    public ResponseEntity<List<ComentarioResponse>> listar(@PathVariable Long chamadoId,
                                                           Authentication auth) {
        return ResponseEntity.ok(comentarioService.listar(chamadoId, auth.getName()));
    }

    @PostMapping
    @Operation(summary = "Adicionar comentário")
    public ResponseEntity<ComentarioResponse> criar(@PathVariable Long chamadoId,
                                                    @Valid @RequestBody ComentarioRequest request,
                                                    Authentication auth) {
        ComentarioResponse criado = comentarioService.criar(chamadoId, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{comentarioId}")
    @Operation(summary = "Editar o próprio comentário")
    public ResponseEntity<ComentarioResponse> editar(@PathVariable Long chamadoId,
                                                     @PathVariable Long comentarioId,
                                                     @Valid @RequestBody ComentarioRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(comentarioService.editar(comentarioId, request, auth.getName()));
    }

    @DeleteMapping("/{comentarioId}")
    @Operation(summary = "Excluir comentário")
    public ResponseEntity<Void> excluir(@PathVariable Long chamadoId,
                                        @PathVariable Long comentarioId,
                                        Authentication auth) {
        comentarioService.excluir(comentarioId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
