package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.AlterarSenhaRequest;
import com.murilo.helpdesk.dto.request.UsuarioRequest;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.ResourceNotFoundException;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.repository.ChamadoRepository;
import com.murilo.helpdesk.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UsuarioService — testes unitários")
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ChamadoRepository chamadoRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UsuarioService usuarioService;

    private Usuario usuario(Long id, String nome, String email, Perfil... perfis) {
        Set<Integer> codigos = new HashSet<>();
        for (Perfil p : perfis) codigos.add(p.getCodigo());

        return Usuario.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .senha("encodedPassword")
                .ativo(true)
                .perfis(codigos)
                .build();
    }

    private UsuarioRequest request(String nome, String email, String senha, Set<Integer> perfis) {
        return new UsuarioRequest(nome, email, senha, null, null, true, perfis);
    }


    @Test
    @DisplayName("findById — usuário existente é retornado")
    void findByIdExistente() {
        var joao = usuario(1L, "João Silva", "joao@test.com", Perfil.CLIENTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(joao));

        assertThat(usuarioService.findById(1L).getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("findById — inexistente lança ResourceNotFoundException (404, não 500)")
    void findByIdInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário");
    }

    @Test
    @DisplayName("listar — filtra por perfil e ordena por nome")
    void listarFiltraPorPerfil() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                usuario(1L, "Zeca", "zeca@test.com", Perfil.CLIENTE),
                usuario(2L, "Ana", "ana@test.com", Perfil.TECNICO),
                usuario(3L, "Bruno", "bruno@test.com", Perfil.CLIENTE)));

        var clientes = usuarioService.listar(Perfil.CLIENTE.getCodigo(), null, null);

        assertThat(clientes).extracting("nome").containsExactly("Bruno", "Zeca");
    }


    @Test
    @DisplayName("create — dados válidos são persistidos com a senha codificada")
    void createValido() {
        var req = request("Maria Souza", "maria@test.com", "senha123",
                Set.of(Perfil.CLIENTE.getCodigo()));

        when(usuarioRepository.existsByEmail("maria@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = usuarioService.create(req);

        assertThat(resultado.email()).isEqualTo("maria@test.com");
        assertThat(resultado.ativo()).isTrue();
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("create — e-mail duplicado é conflito (409)")
    void createEmailDuplicado() {
        var req = request("Duplicado", "dup@test.com", "senha123",
                Set.of(Perfil.CLIENTE.getCodigo()));
        when(usuarioRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.create(req))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).isConflito()).isTrue());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("create — e-mail é normalizado para minúsculas")
    void createNormalizaEmail() {
        var req = request("Maria", "Maria@TEST.com", "senha123",
                Set.of(Perfil.CLIENTE.getCodigo()));

        when(usuarioRepository.existsByEmail("maria@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.create(req).email()).isEqualTo("maria@test.com");
    }

    @Test
    @DisplayName("create — código de perfil inexistente é rejeitado")
    void createPerfilInvalido() {
        var req = request("Fulano", "fulano@test.com", "senha123", Set.of(7));

        assertThatThrownBy(() -> usuarioService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Perfil inválido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePerfis — conjunto vazio é rejeitado")
    void updatePerfisVazio() {
        assertThatThrownBy(() -> usuarioService.updatePerfis(1L, Set.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ao menos um perfil");
    }


    @Test
    @DisplayName("delete — usuário com chamados vinculados não é excluído")
    void deleteComChamados() {
        var joao = usuario(1L, "João", "joao@test.com", Perfil.CLIENTE);
        var admin = usuario(2L, "Admin", "admin@test.com", Perfil.ADMIN);

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(joao));
        when(chamadoRepository.existsByClienteIdOrTecnicoId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.delete(1L, "admin@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Desative a conta");

        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete — usuário sem chamados é removido")
    void deleteSemChamados() {
        var joao = usuario(1L, "João", "joao@test.com", Perfil.CLIENTE);
        var admin = usuario(2L, "Admin", "admin@test.com", Perfil.ADMIN);

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(joao));
        when(chamadoRepository.existsByClienteIdOrTecnicoId(1L, 1L)).thenReturn(false);

        assertThatCode(() -> usuarioService.delete(1L, "admin@test.com"))
                .doesNotThrowAnyException();

        verify(usuarioRepository).delete(joao);
    }

    @Test
    @DisplayName("delete — administrador não exclui a própria conta")
    void deleteProprioUsuario() {
        var admin = usuario(2L, "Admin", "admin@test.com", Perfil.ADMIN);

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> usuarioService.delete(2L, "admin@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("própria conta");
    }

    @Test
    @DisplayName("alterarSituacao — o último administrador ativo não pode ser desativado")
    void naoDesativaUltimoAdmin() {
        var admin = usuario(1L, "Admin", "admin@test.com", Perfil.ADMIN);
        var outroAdmin = usuario(2L, "Root", "root@test.com", Perfil.ADMIN);

        when(usuarioRepository.findByEmail("root@test.com")).thenReturn(Optional.of(outroAdmin));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(usuarioRepository.findAll()).thenReturn(List.of(admin));

        assertThatThrownBy(() -> usuarioService.alterarSituacao(1L, false, "root@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("último administrador");
    }


    @Test
    @DisplayName("alterarSenha — senha atual incorreta é recusada")
    void alterarSenhaAtualErrada() {
        var joao = usuario(1L, "João", "joao@test.com", Perfil.CLIENTE);

        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(joao));
        when(passwordEncoder.matches("errada", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.alterarSenha("joao@test.com",
                new AlterarSenhaRequest("errada", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("senha atual está incorreta");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("alterarSenha — senha atual correta grava a nova senha codificada")
    void alterarSenhaComSucesso() {
        var joao = usuario(1L, "João", "joao@test.com", Perfil.CLIENTE);

        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(joao));
        when(passwordEncoder.matches("atual123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("novaSenha123", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");

        usuarioService.alterarSenha("joao@test.com",
                new AlterarSenhaRequest("atual123", "novaSenha123"));

        assertThat(joao.getSenha()).isEqualTo("novoHash");
        verify(usuarioRepository).save(joao);
    }

    @Test
    @DisplayName("alterarSenha — repetir a senha atual é recusado")
    void alterarSenhaIgualAAtual() {
        var joao = usuario(1L, "João", "joao@test.com", Perfil.CLIENTE);

        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(joao));
        when(passwordEncoder.matches("atual123", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.alterarSenha("joao@test.com",
                new AlterarSenhaRequest("atual123", "atual123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("diferente da atual");
    }
}
