package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.ComentarioRequest;
import com.murilo.helpdesk.dto.response.ComentarioResponse;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.exception.ResourceNotFoundException;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Comentario;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.ComentarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ComentarioService — testes unitários")
class ComentarioServiceTest {

    @Mock private ComentarioRepository comentarioRepository;
    @Mock private ChamadoService chamadoService;
    @Mock private UsuarioService usuarioService;
    @Mock private HistoricoService historicoService;

    @InjectMocks private ComentarioService comentarioService;

    private static final String EMAIL_TECNICO = "tecnico@test.com";
    private static final String EMAIL_CLIENTE = "cliente@test.com";
    private static final String EMAIL_ADMIN   = "admin@test.com";

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
        return Chamado.builder()
                .id(id)
                .numero("CH-2026-000001")
                .titulo("Problema de rede")
                .status(status.getCodigo())
                .cliente(cliente)
                .build();
    }

    private Comentario comentario(Long id, Chamado chamado, Usuario autor) {
        return Comentario.builder()
                .id(id)
                .chamado(chamado)
                .autor(autor)
                .texto("Texto original")
                .build();
    }

    private void devolverOQueForSalvo() {
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));
    }


    @Nested
    @DisplayName("Listagem")
    class Listagem {

        @Test
        @DisplayName("atendente vê também os comentários internos")
        void atendenteVeInternos() {
            Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
            Chamado c = chamado(1L, Status.ABERTO, usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE));

            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
            when(chamadoService.findById(1L)).thenReturn(c);
            when(comentarioRepository.findByChamadoIdOrderByDataCriacaoAsc(1L))
                    .thenReturn(List.of(comentario(5L, c, tecnico)));

            List<ComentarioResponse> lista = comentarioService.listar(1L, EMAIL_TECNICO);

            assertThat(lista).hasSize(1);
            verify(chamadoService).garantirAcessoDeLeitura(c, tecnico);
            verify(comentarioRepository, never()).findByChamadoIdAndInternoFalseOrderByDataCriacaoAsc(any());
        }

        @Test
        @DisplayName("cliente vê apenas os comentários públicos")
        void clienteNaoVeInternos() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Chamado c = chamado(1L, Status.ABERTO, cliente);

            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
            when(chamadoService.findById(1L)).thenReturn(c);
            when(comentarioRepository.findByChamadoIdAndInternoFalseOrderByDataCriacaoAsc(1L))
                    .thenReturn(List.of());

            assertThat(comentarioService.listar(1L, EMAIL_CLIENTE)).isEmpty();
            verify(comentarioRepository, never()).findByChamadoIdOrderByDataCriacaoAsc(any());
        }

        @Test
        @DisplayName("propaga a falta de acesso ao chamado")
        void semAcesso() {
            Usuario cliente = usuario(11L, "outro@test.com", Perfil.CLIENTE);
            Chamado c = chamado(1L, Status.ABERTO, usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE));

            when(usuarioService.findByEmail("outro@test.com")).thenReturn(cliente);
            when(chamadoService.findById(1L)).thenReturn(c);
            doThrow(new OperacaoNaoPermitidaException("Você não tem acesso a este chamado."))
                    .when(chamadoService).garantirAcessoDeLeitura(c, cliente);

            assertThatThrownBy(() -> comentarioService.listar(1L, "outro@test.com"))
                    .isInstanceOf(OperacaoNaoPermitidaException.class);
            verifyNoInteractions(comentarioRepository);
        }
    }


    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("não permite comentar em chamado cancelado")
        void chamadoCancelado() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
            when(chamadoService.findById(1L)).thenReturn(chamado(1L, Status.CANCELADO, cliente));

            assertThatThrownBy(() -> comentarioService.criar(1L,
                    new ComentarioRequest("Oi", false), EMAIL_CLIENTE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cancelado");
            verify(comentarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("cliente não consegue criar comentário interno e não conta como primeira resposta")
        void clienteNaoCriaInterno() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Chamado c = chamado(1L, Status.ABERTO, cliente);
            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
            when(chamadoService.findById(1L)).thenReturn(c);
            devolverOQueForSalvo();

            ComentarioResponse resp = comentarioService.criar(1L,
                    new ComentarioRequest("  Alguma novidade?  ", true), EMAIL_CLIENTE);

            assertThat(resp.interno()).isFalse();
            assertThat(resp.texto()).isEqualTo("Alguma novidade?");
            verify(historicoService).registrarComentario(c, cliente);
            verify(chamadoService, never()).registrarPrimeiraResposta(any());
            verify(chamadoService, never()).salvar(any());
        }

        @Test
        @DisplayName("resposta pública do técnico registra a primeira resposta do chamado")
        void tecnicoRespostaPublica() {
            Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
            Chamado c = chamado(1L, Status.EM_ANDAMENTO, usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE));
            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
            when(chamadoService.findById(1L)).thenReturn(c);
            devolverOQueForSalvo();

            ComentarioResponse resp = comentarioService.criar(1L,
                    new ComentarioRequest("Estamos verificando", null), EMAIL_TECNICO);

            assertThat(resp.interno()).isFalse();
            verify(chamadoService).registrarPrimeiraResposta(c);
            verify(chamadoService).salvar(c);
        }

        @Test
        @DisplayName("nota interna do técnico não conta como primeira resposta")
        void tecnicoNotaInterna() {
            Usuario tecnico = usuario(2L, EMAIL_TECNICO, Perfil.TECNICO);
            Chamado c = chamado(1L, Status.EM_ANDAMENTO, usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE));
            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(tecnico);
            when(chamadoService.findById(1L)).thenReturn(c);
            devolverOQueForSalvo();

            ComentarioResponse resp = comentarioService.criar(1L,
                    new ComentarioRequest("Verificar switch do 2º andar", true), EMAIL_TECNICO);

            ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
            verify(comentarioRepository).save(captor.capture());
            assertThat(captor.getValue().getInterno()).isTrue();
            assertThat(captor.getValue().getAutor()).isSameAs(tecnico);
            assertThat(resp.interno()).isTrue();
            verify(chamadoService, never()).registrarPrimeiraResposta(any());
        }
    }


    @Nested
    @DisplayName("Edição e exclusão")
    class EdicaoExclusao {

        @Test
        @DisplayName("autor edita o próprio comentário")
        void autorEdita() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Comentario existente = comentario(5L, chamado(1L, Status.ABERTO, cliente), cliente);
            when(usuarioService.findByEmail(EMAIL_CLIENTE)).thenReturn(cliente);
            when(comentarioRepository.findById(5L)).thenReturn(Optional.of(existente));
            devolverOQueForSalvo();

            ComentarioResponse resp = comentarioService.editar(5L,
                    new ComentarioRequest(" Texto novo ", null), EMAIL_CLIENTE);

            assertThat(resp.texto()).isEqualTo("Texto novo");
        }

        @Test
        @DisplayName("ninguém edita comentário de outra pessoa, nem admin")
        void naoEditaDeOutro() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Usuario admin = usuario(1L, EMAIL_ADMIN, Perfil.ADMIN);
            Comentario existente = comentario(5L, chamado(1L, Status.ABERTO, cliente), cliente);
            when(usuarioService.findByEmail(EMAIL_ADMIN)).thenReturn(admin);
            when(comentarioRepository.findById(5L)).thenReturn(Optional.of(existente));

            assertThatThrownBy(() -> comentarioService.editar(5L,
                    new ComentarioRequest("Alterado", null), EMAIL_ADMIN))
                    .isInstanceOf(OperacaoNaoPermitidaException.class);
            verify(comentarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("comentário inexistente gera ResourceNotFoundException")
        void comentarioInexistente() {
            when(usuarioService.findByEmail(EMAIL_CLIENTE))
                    .thenReturn(usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE));
            when(comentarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> comentarioService.excluir(99L, EMAIL_CLIENTE))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("admin pode excluir comentário de outra pessoa")
        void adminExclui() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Comentario existente = comentario(5L, chamado(1L, Status.ABERTO, cliente), cliente);
            when(usuarioService.findByEmail(EMAIL_ADMIN)).thenReturn(usuario(1L, EMAIL_ADMIN, Perfil.ADMIN));
            when(comentarioRepository.findById(5L)).thenReturn(Optional.of(existente));

            comentarioService.excluir(5L, EMAIL_ADMIN);

            verify(comentarioRepository).delete(existente);
        }

        @Test
        @DisplayName("técnico não pode excluir comentário de outra pessoa")
        void tecnicoNaoExclui() {
            Usuario cliente = usuario(10L, EMAIL_CLIENTE, Perfil.CLIENTE);
            Comentario existente = comentario(5L, chamado(1L, Status.ABERTO, cliente), cliente);
            when(usuarioService.findByEmail(EMAIL_TECNICO)).thenReturn(usuario(2L, EMAIL_TECNICO, Perfil.TECNICO));
            when(comentarioRepository.findById(5L)).thenReturn(Optional.of(existente));

            assertThatThrownBy(() -> comentarioService.excluir(5L, EMAIL_TECNICO))
                    .isInstanceOf(OperacaoNaoPermitidaException.class);
            verify(comentarioRepository, never()).delete(any());
        }
    }
}
