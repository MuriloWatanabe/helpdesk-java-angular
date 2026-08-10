package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.response.AnexoResponse;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.exception.ResourceNotFoundException;
import com.murilo.helpdesk.model.Anexo;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.HistoricoChamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.AnexoRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AnexoService {


    private static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;

    private final AnexoRepository anexoRepository;
    private final ChamadoService chamadoService;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<AnexoResponse> listar(Long chamadoId, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = chamadoService.findById(chamadoId);
        chamadoService.garantirAcessoDeLeitura(chamado, solicitante);

        List<Anexo> anexos = solicitante.ehAtendente()
                ? anexoRepository.findByChamadoIdOrderByDataUploadDesc(chamadoId)
                : anexoRepository.findByChamadoIdAndPublicoTrueOrderByDataUploadDesc(chamadoId);

        return anexos.stream().map(Mapper::toAnexoResponse).toList();
    }

    @Transactional
    public AnexoResponse enviar(Long chamadoId, MultipartFile arquivo, String descricao,
                                Boolean interno, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = chamadoService.findById(chamadoId);
        chamadoService.garantirAcessoDeLeitura(chamado, solicitante);

        if (chamado.getStatusEnum() == Status.CANCELADO) {
            throw new BusinessException("Chamado cancelado não aceita novos anexos.");
        }
        validar(arquivo);

        String nomeOriginal = sanitizarNome(arquivo.getOriginalFilename());
        String nomeArmazenado = UUID.randomUUID() + extensaoDe(nomeOriginal);

        Path destino = gravarEmDisco(arquivo, nomeArmazenado);

        Anexo anexo = Anexo.builder()
                .chamado(chamado)
                .uploadPor(solicitante)
                .nomeArquivo(nomeOriginal)
                .caminhoArquivo(destino.getFileName().toString())
                .tamanho(arquivo.getSize())
                .tipoMime(arquivo.getContentType())
                .descricao(descricao)

                .publico(!(Boolean.TRUE.equals(interno) && solicitante.ehAtendente()))
                .build();

        Anexo salvo = anexoRepository.save(anexo);
        historicoService.registrar(chamado, solicitante,
                HistoricoChamado.TipoAlteracao.COMENTARIO,
                "Anexo adicionado: " + nomeOriginal);

        return Mapper.toAnexoResponse(salvo);
    }


    @Transactional(readOnly = true)
    public ArquivoBaixado baixar(Long anexoId, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Anexo anexo = buscar(anexoId);
        chamadoService.garantirAcessoDeLeitura(anexo.getChamado(), solicitante);

        if (Boolean.FALSE.equals(anexo.getPublico()) && !solicitante.ehAtendente()) {
            throw new OperacaoNaoPermitidaException("Anexo restrito à equipe de suporte.");
        }

        try {
            Path caminho = diretorioBase().resolve(anexo.getCaminhoArquivo()).normalize();
            if (!caminho.startsWith(diretorioBase())) {
                throw new OperacaoNaoPermitidaException("Caminho de arquivo inválido.");
            }
            Resource recurso = new UrlResource(caminho.toUri());
            if (!recurso.exists() || !recurso.isReadable()) {
                throw new ResourceNotFoundException("Arquivo não encontrado no servidor.");
            }
            return new ArquivoBaixado(recurso, anexo.getNomeArquivo(), anexo.getTipoMime());
        } catch (IOException e) {
            log.error("Falha ao ler anexo {}", anexoId, e);
            throw new BusinessException("Não foi possível ler o arquivo.");
        }
    }

    @Transactional
    public void excluir(Long anexoId, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Anexo anexo = buscar(anexoId);

        boolean autor = anexo.getUploadPor() != null
                && anexo.getUploadPor().getId().equals(solicitante.getId());
        if (!autor && !solicitante.ehAdmin()) {
            throw new OperacaoNaoPermitidaException(
                    "Apenas quem enviou o anexo ou um administrador pode removê-lo.");
        }

        try {
            Files.deleteIfExists(diretorioBase().resolve(anexo.getCaminhoArquivo()).normalize());
        } catch (IOException e) {

            log.warn("Não foi possível remover o arquivo do anexo {}: {}", anexoId, e.getMessage());
        }
        anexoRepository.delete(anexo);
    }


    private void validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Selecione um arquivo para enviar.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new BusinessException("O arquivo excede o limite de 10 MB.");
        }

        Anexo referencia = new Anexo();
        referencia.setTipoMime(arquivo.getContentType());
        if (!referencia.ehTipoPermitido()) {
            throw new BusinessException(
                    "Formato não permitido. Aceitos: imagens, PDF, Word, TXT e CSV.");
        }
    }

    private Path gravarEmDisco(MultipartFile arquivo, String nomeArmazenado) {
        try {
            Path base = diretorioBase();
            Files.createDirectories(base);
            Path destino = base.resolve(nomeArmazenado).normalize();

            if (!destino.startsWith(base)) {
                throw new BusinessException("Nome de arquivo inválido.");
            }
            try (InputStream in = arquivo.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return destino;
        } catch (IOException e) {
            log.error("Falha ao gravar anexo", e);
            throw new BusinessException("Não foi possível salvar o arquivo enviado.");
        }
    }

    private Path diretorioBase() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String sanitizarNome(String original) {
        if (original == null || original.isBlank()) return "arquivo";
        String apenasNome = Paths.get(original).getFileName().toString();
        return apenasNome.replaceAll("[^a-zA-Z0-9._\\- ]", "_");
    }

    private String extensaoDe(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto > 0 ? nome.substring(ponto) : "";
    }

    private Anexo buscar(Long id) {
        return anexoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Anexo", id));
    }


    public record ArquivoBaixado(Resource recurso, String nomeArquivo, String tipoMime) {}
}
