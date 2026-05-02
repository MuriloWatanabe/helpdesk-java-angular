package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.UsuarioRequest;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — testes unitários")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario buildUsuario(Long id, String nome, String email) {
        return Usuario.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .senha("encodedPassword")
                .perfis(new HashSet<>(Set.of(Perfil.CLIENTE.getCodigo())))
                .build();
    }

    // ──────────────────────────────────────────────────────────
    // findById
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById — quando usuário existe deve retornar o usuário")
    void findById_quandoExiste_retornaUsuario() {
        var usuario = buildUsuario(1L, "João Silva", "joao@test.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        var result = usuarioService.findById(1L);

        assertThat(result).isEqualTo(usuario);
        assertThat(result.getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("findById — quando usuário não existe deve lançar RuntimeException")
    void findById_quandoNaoExiste_lancaRuntimeException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    // ──────────────────────────────────────────────────────────
    // create
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("create — com dados válidos deve salvar e retornar UsuarioResponse")
    void create_comDadosValidos_retornaResponse() {
        var request = new UsuarioRequest("Maria Souza", "maria@test.com", "senha123",
                Set.of(Perfil.CLIENTE.getCodigo()));
        var usuarioSalvo = buildUsuario(2L, "Maria Souza", "maria@test.com");

        when(usuarioRepository.existsByEmail("maria@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        var result = usuarioService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("maria@test.com");
        assertThat(result.nome()).isEqualTo("Maria Souza");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("create — com email duplicado deve lançar RuntimeException")
    void create_comEmailDuplicado_lancaRuntimeException() {
        var request = new UsuarioRequest("Duplicado", "dup@test.com", "senha123", null);
        when(usuarioRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────────────────
    // delete
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete — quando usuário existe deve deletar sem exceção")
    void delete_quandoExiste_deletaSemExcecao() {
        var usuario = buildUsuario(1L, "João", "joao@test.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).deleteById(1L);

        assertThatCode(() -> usuarioService.delete(1L)).doesNotThrowAnyException();
        verify(usuarioRepository).deleteById(1L);
    }

    // ──────────────────────────────────────────────────────────
    // findAll
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll — deve retornar lista mapeada de UsuarioResponse")
    void findAll_retornaListaMapeada() {
        var usuarios = List.of(
                buildUsuario(1L, "João", "joao@test.com"),
                buildUsuario(2L, "Maria", "maria@test.com")
        );
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        var result = usuarioService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("email")
                .containsExactlyInAnyOrder("joao@test.com", "maria@test.com");
    }
}
