package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.AvaliacaoRequest;
import com.murilo.helpdesk.dto.response.AvaliacaoResponse;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.model.Avaliacao;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.HistoricoChamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.AvaliacaoRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ChamadoService chamadoService;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public Optional<AvaliacaoResponse> buscarPorChamado(Long chamadoId, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = chamadoService.findById(chamadoId);
        chamadoService.garantirAcessoDeLeitura(chamado, solicitante);

        return avaliacaoRepository.findByChamadoId(chamadoId).map(Mapper::toAvaliacaoResponse);
    }

    @Transactional
    public AvaliacaoResponse avaliar(Long chamadoId, AvaliacaoRequest request,
                                     String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = chamadoService.findById(chamadoId);


        if (chamado.getCliente() == null
                || !chamado.getCliente().getId().equals(solicitante.getId())) {
            throw new OperacaoNaoPermitidaException(
                    "Apenas o cliente que abriu o chamado pode avaliar o atendimento.");
        }

        Status status = chamado.getStatusEnum();
        if (status != Status.RESOLVIDO && status != Status.ENCERRADO) {
            throw new BusinessException(
                    "A avaliação fica disponível quando o chamado for resolvido.");
        }
        if (avaliacaoRepository.existsByChamadoId(chamadoId)) {
            throw BusinessException.conflito("Este chamado já foi avaliado.");
        }

        Avaliacao avaliacao = Avaliacao.builder()
                .chamado(chamado)
                .usuarioAvaliador(solicitante)
                .nota(request.nota())
                .comentario(request.comentario())
                .aspectosAvaliados(request.aspectos() == null
                        ? new HashSet<>() : new HashSet<>(request.aspectos()))
                .build();

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        historicoService.registrar(chamado, solicitante,
                HistoricoChamado.TipoAlteracao.COMENTARIO,
                "Atendimento avaliado com nota " + request.nota());

        log.info("Chamado {} avaliado com nota {}", chamado.getNumero(), request.nota());
        return Mapper.toAvaliacaoResponse(salva);
    }
}
