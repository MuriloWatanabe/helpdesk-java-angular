package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.response.HistoricoResponse;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.HistoricoChamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.HistoricoChamadoRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Registra a linha do tempo do chamado. Toda mudança relevante passa por aqui,
 * de modo que o cliente consegue auditar o que aconteceu e quando.
 */
@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final HistoricoChamadoRepository historicoRepository;

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listar(Long chamadoId) {
        return historicoRepository.findByChamadoIdOrderByDataAlteracaoDesc(chamadoId).stream()
                .map(Mapper::toHistoricoResponse)
                .toList();
    }

    @Transactional
    public void registrarCriacao(Chamado chamado, Usuario autor) {
        historicoRepository.save(HistoricoChamado.criarCriacao(chamado, autor));
    }

    @Transactional
    public void registrarMudancaStatus(Chamado chamado, Usuario autor,
                                       String anterior, String novo) {
        historicoRepository.save(
                HistoricoChamado.criarMudancaStatus(chamado, autor, anterior, novo));
    }

    @Transactional
    public void registrarMudancaPrioridade(Chamado chamado, Usuario autor,
                                           String anterior, String nova) {
        historicoRepository.save(
                HistoricoChamado.criarMudancaPrioridade(chamado, autor, anterior, nova));
    }

    @Transactional
    public void registrarAtribuicaoTecnico(Chamado chamado, Usuario autor,
                                           String anterior, String novo) {
        historicoRepository.save(
                HistoricoChamado.criarAtribuicaoTecnico(chamado, autor, anterior, novo));
    }

    @Transactional
    public void registrarComentario(Chamado chamado, Usuario autor) {
        historicoRepository.save(HistoricoChamado.criarComentarioAdicionado(chamado, autor));
    }

    @Transactional
    public void registrarFechamento(Chamado chamado, Usuario autor) {
        historicoRepository.save(HistoricoChamado.criarFechamento(chamado, autor));
    }

    @Transactional
    public void registrarReabertura(Chamado chamado, Usuario autor) {
        historicoRepository.save(HistoricoChamado.criarReabertura(chamado, autor));
    }

    /** Evento livre — usado por anexos e avaliação. */
    @Transactional
    public void registrar(Chamado chamado, Usuario autor,
                          HistoricoChamado.TipoAlteracao tipo, String descricao) {
        historicoRepository.save(HistoricoChamado.builder()
                .chamado(chamado)
                .usuarioAlteracao(autor)
                .tipoAlteracao(tipo)
                .descricao(descricao)
                .build());
    }
}
