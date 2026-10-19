package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.AvaliacaoRequest;
import com.murilo.helpdesk.dto.response.AvaliacaoResponse;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.model.Avaliacao;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.HistoricoChamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.AvaliacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AvaliacaoService — testes unitários")
class AvaliacaoServiceTest {

    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private ChamadoService chamadoService;
    @Mock private UsuarioService usuarioService;
    @Mock private HistoricoService historicoService;

    @InjectMocks private AvaliacaoService avaliacaoService;

    private static final String EMAIL_CLIENTE = "cliente@test.com";
    private static final String EMAIL_OUTRO   = "outro@test.com";

    private final Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);

    private Usuario usuario(Long id, String email, Perfil perfil) {
        return Usuario.builder()
                .id(id)
                .nome("Usuário " + id)
                .email(email)
                .senha("encoded")
                .ativo(true)
                .perfis(new HashSet<>(Set.of(perfil.getCodigo())))
                .build();
    }

    private Chamado chamado(Status status, Usuario dono) {
        return Chamado.builder()
                .id(1L)
                .numero("CH-2026-000001")
                .titulo("Problema de rede")
                .status(status.getCodigo())
                .cliente(dono)
                .build();
    }

    @Test
    @DisplayName("cliente avalia o próprio chamado resolvido")
    void avaliaChamadoResolvido() {
        Chamado c = chamado(Status.RESOLVIDO, cliente);
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoService.findById(1L)).thenReturn(c);
        when(avaliacaoRepository.existsByChamadoId(1L)).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoResponse resp = avaliacaoService.avaliar(1L,
                new AvaliacaoRequest(5, "Ótimo atendimento", Set.of("RAPIDEZ")), EMAIL_CLIENTE);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuarioAvaliador()).isSameAs(cliente);
        assertThat(captor.getValue().getAspectosAvaliados()).containsExactly("RAPIDEZ");
        assertThat(resp.nota()).isEqualTo(5);
        verify(historicoService).registrar(eq(c), eq(cliente),
                eq(HistoricoChamado.TipoAlteracao.COMENTARIO), contains("nota 5"));
    }

    @Test
    @DisplayName("aspectos nulos viram um conjunto vazio")
    void aspectosNulos() {
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoService.findById(1L)).thenReturn(chamado(Status.ENCERRADO, cliente));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> inv.getArgument(0));

        avaliacaoService.avaliar(1L, new AvaliacaoRequest(3, null, null), EMAIL_CLIENTE);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getAspectosAvaliados()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("somente o cliente dono do chamado pode avaliar")
    void outroUsuarioNaoAvalia() {
        when(usuarioService.findByEmail(EMAIL_OUTRO)).thenReturn(usuario(11L, EMAIL_OUTRO, Perfil.CLIENTE));
        when(chamadoService.findById(1L)).thenReturn(chamado(Status.RESOLVIDO, cliente));

        assertThatThrownBy(() -> avaliacaoService.avaliar(1L,
                new AvaliacaoRequest(5, null, null), EMAIL_OUTRO))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("chamado sem cliente não pode ser avaliado")
    void chamadoSemCliente() {
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoService.findById(1L)).thenReturn(chamado(Status.RESOLVIDO, null));

        assertThatThrownBy(() -> avaliacaoService.avaliar(1L,
                new AvaliacaoRequest(5, null, null), EMAIL_CLIENTE))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @ParameterizedTest(name = "status {0} não permite avaliação")
    @EnumSource(value = Status.class, names = {"RESOLVIDO", "ENCERRADO"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("só avalia chamados resolvidos ou encerrados")
    void statusNaoPermiteAvaliacao(Status status) {
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoService.findById(1L)).thenReturn(chamado(status, cliente));

        assertThatThrownBy(() -> avaliacaoService.avaliar(1L,
                new AvaliacaoRequest(4, null, null), EMAIL_CLIENTE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("resolvido");
    }

    @Test
    @DisplayName("não permite avaliar o mesmo chamado duas vezes")
    void avaliacaoDuplicada() {
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoService.findById(1L)).thenReturn(chamado(Status.RESOLVIDO, cliente));
        when(avaliacaoRepository.existsByChamadoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> avaliacaoService.avaliar(1L,
                new AvaliacaoRequest(4, null, null), EMAIL_CLIENTE))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.isConflito()).isTrue());
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorChamado valida acesso e retorna vazio quando não há avaliação")
    void buscarSemAvaliacao() {
        Chamado c = chamado(Status.RESOLVIDO, cliente);
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoService.findById(1L)).thenReturn(c);
        when(avaliacaoRepository.findByChamadoId(1L)).thenReturn(Optional.empty());

        assertThat(avaliacaoService.buscarPorChamado(1L, EMAIL_CLIENTE)).isEmpty();
        verify(chamadoService).garantirAcessoDeLeitura(c, cliente);
    }

    @Test
    @DisplayName("buscarPorChamado retorna a avaliação existente")
    void buscarComAvaliacao() {
        Chamado c = chamado(Status.RESOLVIDO, cliente);
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoService.findById(1L)).thenReturn(c);
        when(avaliacaoRepository.findByChamadoId(1L)).thenReturn(Optional.of(
                Avaliacao.builder().id(7L).chamado(c).nota(4).usuarioAvaliador(cliente).build()));

        Optional<AvaliacaoResponse> resp = avaliacaoService.buscarPorChamado(1L, EMAIL_CLIENTE);

        assertThat(resp).isPresent();
        assertThat(resp.get().nota()).isEqualTo(4);
        assertThat(resp.get().chamadoId()).isEqualTo(1L);
    }
}
