package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.response.HistoricoResponse;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.HistoricoChamado;
import com.murilo.helpdesk.model.HistoricoChamado.TipoAlteracao;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.HistoricoChamadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoricoService — testes unitários")
class HistoricoServiceTest {

    @Mock private HistoricoChamadoRepository historicoRepository;

    @InjectMocks private HistoricoService historicoService;

    private final Chamado chamado = Chamado.builder().id(1L).numero("CH-2026-000001").build();
    private final Usuario autor = Usuario.builder().id(2L).nome("Técnico").email("t@test.com").build();

    private HistoricoChamado salvo() {
        ArgumentCaptor<HistoricoChamado> captor = ArgumentCaptor.forClass(HistoricoChamado.class);
        verify(historicoRepository).save(captor.capture());
        HistoricoChamado h = captor.getValue();
        assertThat(h.getChamado()).isSameAs(chamado);
        assertThat(h.getUsuarioAlteracao()).isSameAs(autor);
        return h;
    }

    @Test
    @DisplayName("registra criação")
    void criacao() {
        historicoService.registrarCriacao(chamado, autor);
        assertThat(salvo().getTipoAlteracao()).isEqualTo(TipoAlteracao.CRIACAO);
    }

    @Test
    @DisplayName("registra mudança de status com valores anterior e novo")
    void mudancaStatus() {
        historicoService.registrarMudancaStatus(chamado, autor, "Aberto", "Em atendimento");
        HistoricoChamado h = salvo();
        assertThat(h.getTipoAlteracao()).isEqualTo(TipoAlteracao.STATUS);
        assertThat(h.getValorAnterior()).isEqualTo("Aberto");
        assertThat(h.getValorNovo()).isEqualTo("Em atendimento");
    }

    @Test
    @DisplayName("registra mudança de prioridade")
    void mudancaPrioridade() {
        historicoService.registrarMudancaPrioridade(chamado, autor, "Baixa", "Alta");
        HistoricoChamado h = salvo();
        assertThat(h.getTipoAlteracao()).isEqualTo(TipoAlteracao.PRIORIDADE);
        assertThat(h.getValorNovo()).isEqualTo("Alta");
    }

    @Test
    @DisplayName("registra atribuição de técnico")
    void atribuicaoTecnico() {
        historicoService.registrarAtribuicaoTecnico(chamado, autor, null, "Técnico");
        assertThat(salvo().getTipoAlteracao()).isEqualTo(TipoAlteracao.TECNICO);
    }

    @Test
    @DisplayName("registra comentário, fechamento e reabertura")
    void outrosEventos() {
        historicoService.registrarComentario(chamado, autor);
        historicoService.registrarFechamento(chamado, autor);
        historicoService.registrarReabertura(chamado, autor);

        ArgumentCaptor<HistoricoChamado> captor = ArgumentCaptor.forClass(HistoricoChamado.class);
        verify(historicoRepository, times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(HistoricoChamado::getTipoAlteracao)
                .containsExactly(TipoAlteracao.COMENTARIO, TipoAlteracao.FECHAMENTO, TipoAlteracao.REABERTURA);
    }

    @Test
    @DisplayName("registro genérico usa tipo e descrição informados")
    void registroGenerico() {
        historicoService.registrar(chamado, autor, TipoAlteracao.TITULO, "Título alterado");
        HistoricoChamado h = salvo();
        assertThat(h.getTipoAlteracao()).isEqualTo(TipoAlteracao.TITULO);
        assertThat(h.getDescricao()).isEqualTo("Título alterado");
    }

    @Test
    @DisplayName("lista o histórico do chamado convertido em resposta")
    void listar() {
        when(historicoRepository.findByChamadoIdOrderByDataAlteracaoDesc(1L))
                .thenReturn(List.of(HistoricoChamado.criarCriacao(chamado, autor)));

        List<HistoricoResponse> lista = historicoService.listar(1L);

        assertThat(lista).hasSize(1);
    }
}
