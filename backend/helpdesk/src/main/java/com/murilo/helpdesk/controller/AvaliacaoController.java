package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.request.AvaliacaoRequest;
import com.murilo.helpdesk.dto.response.AvaliacaoResponse;
import com.murilo.helpdesk.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chamados/{chamadoId}/avaliacao")
@Tag(name = "Avaliação", description = "Pesquisa de satisfação do atendimento")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;


    @GetMapping
    @Operation(summary = "Consultar avaliação do chamado")
    public ResponseEntity<AvaliacaoResponse> buscar(@PathVariable Long chamadoId,
                                                    Authentication auth) {
        return avaliacaoService.buscarPorChamado(chamadoId, auth.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @Operation(summary = "Avaliar o atendimento (cliente do chamado)")
    public ResponseEntity<AvaliacaoResponse> avaliar(@PathVariable Long chamadoId,
                                                     @Valid @RequestBody AvaliacaoRequest request,
                                                     Authentication auth) {
        AvaliacaoResponse criada = avaliacaoService.avaliar(chamadoId, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
}
