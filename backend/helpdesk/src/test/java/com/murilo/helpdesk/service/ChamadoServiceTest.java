package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.ChamadoRequest;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.exception.ResourceNotFoundException;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Categoria;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.AnexoRepository;
import com.murilo.helpdesk.repository.AvaliacaoRepository;
import com.murilo.helpdesk.repository.ChamadoRepository;
import com.murilo.helpdesk.repository.ComentarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ChamadoService — testes unitários")
class ChamadoServiceTest {

    @Mock private ChamadoRepository chamadoRepository;
    @Mock private ComentarioRepository comentarioRepository;
    @Mock private AnexoRepository anexoRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private HistoricoService historicoService;

    @InjectMocks private ChamadoService chamadoService;

    private static final String EMAIL_ADMIN   = "admin@test.com";
    private static final String EMAIL_TECNICO = "tecnico@test.com";
    private static final String EMAIL_CLIENTE = "cliente@test.com";
    private static final String EMAIL_OUTRO   = "outro@test.com";

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

    private Chamado chamado(Long id, Status status, Usuario cliente) {
        Chamado c = Chamado.builder()
                .id(id)
                .numero("CH-2026-000001")
                .titulo("Problema de rede")
                .observacoes("Sem acesso à internet desde hoje cedo")
                .status(status.getCodigo())
                .prioridade(Prioridade.ALTA.getCodigo())
                .categoria(Categoria.REDE.getCodigo())
                .cliente(cliente)
                .dataAbertura(LocalDateTime.now().minusHours(1))
                .build();
        c.calcularPrazoSla();
        return c;
    }


    @Nested
    @DisplayName("Controle de acesso")
    class ControleDeAcesso {

        @Test
        @DisplayName("cliente não pode abrir o chamado de outro cliente")
        void clienteNaoAcessaChamadoDeTerceiro() {
            Usuario dono = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Usuario intruso = usuario(11L, EMAIL_OUTRO, Perfil.CLIENTE);

            when(usuarioService.findByEmail(EMAIL_OUTRO)).thenReturn(intruso);
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado(1L, Status.ABERTO, dono)));

            assertThatThrownBy(() -> chamadoService.buscarPorId(1L, EMAIL_OUTRO))
                    .isInstanceOf(OperacaoNaoPermitidaException.class)
                    .hasMessageContaining("não tem acesso");
        }

        @Test
        @DisplayName("cliente acessa o próprio chamado")
        void clienteAcessaProprioChamado() {
            Usuario dono = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);

            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(dono);
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado(1L, Status.ABERTO, dono)));

            var resposta = chamadoService.buscarPorId(1L, EMAIL_CLIENTE);

            assertThat(resposta.titulo()).isEqualTo("Problema de rede");
        }

        @Test
        @DisplayName("técnico acessa chamado de qualquer cliente")
        void tecnicoAcessaQualquerChamado() {
            Usuario dono = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);

            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado(1L, Status.ABERTO, dono)));

            assertThatCode(() -> chamadoService.buscarPorId(1L, EMAIL_TECNICO))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("findById — chamado inexistente lança ResourceNotFoundException")
    void findByIdInexistente() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Chamado");
    }


    @Test
    @DisplayName("create — chamado nasce ABERTO, com protocolo e prazo de SLA")
    void criarGeraProtocoloEPrazo() {
        Usuario admin = usuario(1L, EMAIL_ADMIN, Perfil.ADMIN);
        Usuario cliente = usuario(5L, EMAIL_CLIENTE, Perfil.CLIENTE);

        when(usuarioService.findByEmail(EMAIL_ADMIN)).thenReturn(admin);
        when(usuarioService.findById(5L)).thenReturn(cliente);
        when(chamadoRepository.findUltimoNumeroDoAno(anyString())).thenReturn(null);
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new ChamadoRequest("Problema de rede",
                "Sem acesso à internet desde hoje cedo",
                Prioridade.ALTA.getCodigo(), Categoria.REDE.getCodigo(), null, 5L);

        var resposta = chamadoService.criar(request, EMAIL_ADMIN);

        assertThat(resposta.status()).isEqualTo(Status.ABERTO.getCodigo());
        assertThat(resposta.numero()).endsWith("-000001");
        verify(historicoService).registrarCriacao(any(Chamado.class), eq(admin));
    }

    @Test
    @DisplayName("create — cliente sempre abre em seu próprio nome")
    void clienteAbreParaSiMesmo() {
        Usuario cliente = usuario(5L, EMAIL_CLIENTE, Perfil.CLIENTE);
        Usuario terceiro = usuario(9L, EMAIL_OUTRO, Perfil.CLIENTE);

        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(usuarioService.findById(5L)).thenReturn(cliente);
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));


        var request = new ChamadoRequest("Problema de rede",
                "Sem acesso à internet desde hoje cedo",
                Prioridade.MEDIA.getCodigo(), Categoria.REDE.getCodigo(), null, terceiro.getId());

        var resposta = chamadoService.criar(request, EMAIL_CLIENTE);

        assertThat(resposta.cliente().id()).isEqualTo(5L);
        verify(usuarioService, never()).findById(9L);
    }

    @Test
    @DisplayName("create — prioridade inválida vira erro de negócio, não 500")
    void prioridadeInvalida() {
        Usuario cliente = usuario(5L, EMAIL_CLIENTE, Perfil.CLIENTE);
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);

        var request = new ChamadoRequest("Problema de rede",
                "Sem acesso à internet desde hoje cedo", 99, Categoria.REDE.getCodigo(), null, null);

        assertThatThrownBy(() -> chamadoService.criar(request, EMAIL_CLIENTE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Prioridade inválida");
    }


    @Nested
    @DisplayName("Alteração de status")
    class AlteracaoDeStatus {

        @Test
        @DisplayName("encerrar define a data de fechamento")
        void encerrarDefineDataFechamento() {
            Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Chamado alvo = chamado(1L, Status.RESOLVIDO, cliente);

            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

            var resposta = chamadoService.alterarStatus(1L, Status.ENCERRADO, EMAIL_TECNICO);

            assertThat(alvo.getStatus()).isEqualTo(Status.ENCERRADO.getCodigo());
            assertThat(alvo.getDataFechamento()).isNotNull();
            assertThat(resposta.encerrado()).isTrue();
        }

        @Test
        @DisplayName("ir para EM_ANDAMENTO não define data de fechamento")
        void emAndamentoNaoFecha() {
            Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Chamado alvo = chamado(1L, Status.ABERTO, cliente);

            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

            chamadoService.alterarStatus(1L, Status.EM_ANDAMENTO, EMAIL_TECNICO);

            assertThat(alvo.getStatus()).isEqualTo(Status.EM_ANDAMENTO.getCodigo());
            assertThat(alvo.getDataFechamento()).isNull();
            assertThat(alvo.getDataPrimeiraResposta()).isNotNull();
        }

        @Test
        @DisplayName("transição incoerente é recusada")
        void transicaoInvalida() {
            Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);

            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
            when(chamadoRepository.findById(1L))
                    .thenReturn(Optional.of(chamado(1L, Status.CANCELADO, cliente)));

            assertThatThrownBy(() ->
                    chamadoService.alterarStatus(1L, Status.EM_ANDAMENTO, EMAIL_TECNICO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Não é possível mudar");
        }

        @Test
        @DisplayName("cliente pode confirmar a solução do próprio chamado")
        void clienteConfirmaSolucao() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Chamado alvo = chamado(1L, Status.RESOLVIDO, cliente);

            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

            chamadoService.alterarStatus(1L, Status.ENCERRADO, EMAIL_CLIENTE);

            assertThat(alvo.getStatus()).isEqualTo(Status.ENCERRADO.getCodigo());
        }

        @Test
        @DisplayName("cliente não pode marcar o próprio chamado como resolvido")
        void clienteNaoResolveSozinho() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);

            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
            when(chamadoRepository.findById(1L))
                    .thenReturn(Optional.of(chamado(1L, Status.ABERTO, cliente)));

            assertThatThrownBy(() ->
                    chamadoService.alterarStatus(1L, Status.RESOLVIDO, EMAIL_CLIENTE))
                    .isInstanceOf(OperacaoNaoPermitidaException.class);
        }

        @Test
        @DisplayName("reabertura limpa a data de fechamento")
        void reaberturaLimpaFechamento() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Chamado alvo = chamado(1L, Status.ENCERRADO, cliente);
            alvo.setDataFechamento(LocalDateTime.now().minusDays(1));

            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

            chamadoService.alterarStatus(1L, Status.EM_ANDAMENTO, EMAIL_CLIENTE);

            assertThat(alvo.getDataFechamento()).isNull();
            verify(historicoService).registrarReabertura(eq(alvo), eq(cliente));
        }
    }


    @Test
    @DisplayName("assumir — técnico vira responsável e o chamado entra em andamento")
    void assumirColocaEmAndamento() {
        Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
        Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
        Chamado alvo = chamado(1L, Status.ABERTO, cliente);

        when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        chamadoService.assumir(1L, EMAIL_TECNICO);

        assertThat(alvo.getTecnico()).isEqualTo(tecnico);
        assertThat(alvo.getStatus()).isEqualTo(Status.EM_ANDAMENTO.getCodigo());
    }

    @Test
    @DisplayName("assumir — chamado já atribuído a outro técnico é recusado")
    void assumirChamadoDeOutro() {
        Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
        Usuario outroTecnico = usuario(3L, "outro.tec@test.com", Perfil.TECNICO);
        Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);

        Chamado alvo = chamado(1L, Status.EM_ANDAMENTO, cliente);
        alvo.setTecnico(outroTecnico);

        when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> chamadoService.assumir(1L, EMAIL_TECNICO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já atribuído");
    }

    @Test
    @DisplayName("assumir — cliente não pode assumir chamados")
    void clienteNaoAssume() {
        Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);

        assertThatThrownBy(() -> chamadoService.assumir(1L, EMAIL_CLIENTE))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("atribuirTecnico — admin desassume e o chamado volta para a fila")
    void adminDesassumeChamado() {
        Usuario admin = usuario(1L, EMAIL_ADMIN, Perfil.ADMIN);
        Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);

        Chamado alvo = chamado(1L, Status.EM_ANDAMENTO, cliente);
        alvo.setTecnico(admin);

        when(usuarioService.findByEmail(EMAIL_ADMIN)).thenReturn(admin);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        chamadoService.atribuirTecnico(1L, null, EMAIL_ADMIN);

        assertThat(alvo.getTecnico()).isNull();
        verify(historicoService).registrarAtribuicaoTecnico(eq(alvo), eq(admin),
                eq(admin.getNome()), eq("—"));
    }

    @Test
    @DisplayName("atribuirTecnico — cliente não pode mexer no responsável")
    void clienteNaoAlteraResponsavel() {
        Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
        Chamado alvo = chamado(1L, Status.EM_ANDAMENTO, cliente);

        when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> chamadoService.atribuirTecnico(1L, null, EMAIL_CLIENTE))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }


    @Test
    @DisplayName("excluir — apenas administrador")
    void excluirSomenteAdmin() {
        Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
        when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);

        assertThatThrownBy(() -> chamadoService.excluir(1L, EMAIL_TECNICO))
                .isInstanceOf(OperacaoNaoPermitidaException.class);


        verify(chamadoRepository, never()).delete(any(Chamado.class));
    }

    @Test
    @DisplayName("excluir — chamado inexistente lança 404")
    void excluirInexistente() {
        Usuario admin = usuario(1L, EMAIL_ADMIN, Perfil.ADMIN);
        when(usuarioService.findByEmail(EMAIL_ADMIN)).thenReturn(admin);
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.excluir(99L, EMAIL_ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(chamadoRepository, never()).delete(any(Chamado.class));
    }


    @Test
    @DisplayName("SLA — chamado urgente aberto há muito tempo aparece como vencido")
    void slaVencido() {
        Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
        Chamado alvo = chamado(1L, Status.EM_ANDAMENTO, cliente);
        alvo.setPrioridadeEnum(Prioridade.URGENTE);
        alvo.setDataAbertura(LocalDateTime.now().minusDays(2));
        alvo.calcularPrazoSla();

        assertThat(alvo.isSlaVencido()).isTrue();
        assertThat(alvo.getHorasRestantesSla()).isNegative();
    }

    @Test
    @DisplayName("SLA — chamado encerrado não conta mais como vencido")
    void slaNaoContaAposEncerrar() {
        Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
        Chamado alvo = chamado(1L, Status.ENCERRADO, cliente);
        alvo.setDataAbertura(LocalDateTime.now().minusDays(30));
        alvo.calcularPrazoSla();

        assertThat(alvo.isSlaVencido()).isFalse();
    }
}
