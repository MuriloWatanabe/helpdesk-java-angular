package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.ChamadoRequest;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.ChamadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChamadoService — testes unitários")
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ChamadoService chamadoService;

    private Usuario buildUsuario(Long id, Perfil perfil) {
        return Usuario.builder()
                .id(id)
                .nome("Usuário Teste")
                .email("usuario@test.com")
                .senha("encoded")
                .perfis(new HashSet<>(Set.of(perfil.getCodigo())))
                .build();
    }

    private Chamado buildChamado(Long id, Status status) {
        var cliente = buildUsuario(10L, Perfil.CLIENTE);
        return Chamado.builder()
                .id(id)
                .titulo("Problema de rede")
                .observacoes("Sem acesso à internet desde hoje cedo")
                .status(status.getCodigo())
                .prioridade(Prioridade.ALTA.getCodigo())
                .cliente(cliente)
                .build();
    }

    // ──────────────────────────────────────────────────────────
    // findById
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById — quando chamado não existe deve lançar RuntimeException")
    void findById_quandoNaoExiste_lancaRuntimeException() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Chamado não encontrado");
    }

    @Test
    @DisplayName("findById — quando chamado existe deve retornar o chamado")
    void findById_quandoExiste_retornaChamado() {
        var chamado = buildChamado(1L, Status.ABERTO);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

        var result = chamadoService.findById(1L);

        assertThat(result).isEqualTo(chamado);
        assertThat(result.getTitulo()).isEqualTo("Problema de rede");
    }

    // ──────────────────────────────────────────────────────────
    // updateStatus
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus — ao encerrar deve setar dataFechamento e status ENCERRADO")
    void updateStatus_quandoEncerrado_setaDataFechamento() {
        var chamado = buildChamado(1L, Status.ABERTO);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = chamadoService.updateStatus(1L, Status.ENCERRADO);

        assertThat(chamado.getStatus()).isEqualTo(Status.ENCERRADO.getCodigo());
        assertThat(chamado.getDataFechamento()).isNotNull();
        assertThat(result.status()).isEqualTo(Status.ENCERRADO.getCodigo());
    }

    @Test
    @DisplayName("updateStatus — ao mover para EM_ANDAMENTO não deve setar dataFechamento")
    void updateStatus_emAndamento_naoSetaDataFechamento() {
        var chamado = buildChamado(1L, Status.ABERTO);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));

        chamadoService.updateStatus(1L, Status.EM_ANDAMENTO);

        assertThat(chamado.getStatus()).isEqualTo(Status.EM_ANDAMENTO.getCodigo());
        assertThat(chamado.getDataFechamento()).isNull();
    }

    // ──────────────────────────────────────────────────────────
    // create
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("create — admin criando chamado para cliente deve persistir corretamente")
    void create_adminCriandoChamado_salvaChamado() {
        var admin = buildUsuario(1L, Perfil.ADMIN);
        var cliente = buildUsuario(5L, Perfil.CLIENTE);
        var chamadoSalvo = buildChamado(10L, Status.ABERTO);

        when(usuarioService.findByEmail("admin@test.com")).thenReturn(admin);
        when(usuarioService.findById(5L)).thenReturn(cliente);
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamadoSalvo);

        var request = new ChamadoRequest("Problema de rede",
                "Sem acesso à internet desde hoje cedo", Prioridade.ALTA.getCodigo(), null, 5L);

        var result = chamadoService.create(request, "admin@test.com");

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(Status.ABERTO.getCodigo());
        verify(chamadoRepository).save(any(Chamado.class));
    }

    // ──────────────────────────────────────────────────────────
    // delete
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete — quando chamado não existe deve lançar RuntimeException")
    void delete_quandoNaoExiste_lancaRuntimeException() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.delete(99L))
                .isInstanceOf(RuntimeException.class);

        verify(chamadoRepository, never()).deleteById(any());
    }
}
