package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.response.AnexoResponse;
import com.murilo.helpdesk.service.AnexoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/v1/chamados/{chamadoId}/anexos")
@Tag(name = "Anexos", description = "Arquivos e evidências do chamado")
@RequiredArgsConstructor
public class AnexoController {

    private final AnexoService anexoService;

    @GetMapping
    @Operation(summary = "Listar anexos do chamado")
    public ResponseEntity<List<AnexoResponse>> listar(@PathVariable Long chamadoId,
                                                      Authentication auth) {
        return ResponseEntity.ok(anexoService.listar(chamadoId, auth.getName()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar anexo (máx. 10 MB)")
    public ResponseEntity<AnexoResponse> enviar(
            @PathVariable Long chamadoId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Boolean interno,
            Authentication auth) {

        AnexoResponse criado = anexoService.enviar(chamadoId, arquivo, descricao, interno,
                auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/{anexoId}/download")
    @Operation(summary = "Baixar anexo")
    public ResponseEntity<Resource> baixar(@PathVariable Long chamadoId,
                                           @PathVariable Long anexoId,
                                           Authentication auth) {
        var arquivo = anexoService.baixar(anexoId, auth.getName());

        // filename* garante acentos corretos no nome sugerido pelo navegador.
        String nomeCodificado = java.net.URLEncoder.encode(arquivo.nomeArquivo(),
                StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + nomeCodificado)
                .contentType(arquivo.tipoMime() != null
                        ? MediaType.parseMediaType(arquivo.tipoMime())
                        : MediaType.APPLICATION_OCTET_STREAM)
                .body(arquivo.recurso());
    }

    @DeleteMapping("/{anexoId}")
    @Operation(summary = "Excluir anexo")
    public ResponseEntity<Void> excluir(@PathVariable Long chamadoId,
                                        @PathVariable Long anexoId,
                                        Authentication auth) {
        anexoService.excluir(anexoId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
