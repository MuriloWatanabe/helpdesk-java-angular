package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.RedefinirSenhaRequest;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.model.PasswordResetToken;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PasswordResetService — testes unitários")
class PasswordResetServiceTest {

    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private UsuarioService usuarioService;

    @InjectMocks private PasswordResetService passwordResetService;

    private static final String EMAIL = "cliente@test.com";

    @BeforeEach
    void configurarPropriedades() {
        ReflectionTestUtils.setField(passwordResetService, "exporLink", true);
        ReflectionTestUtils.setField(passwordResetService, "frontUrl", "http://front");
    }

    private Usuario usuario(boolean ativo) {
        return Usuario.builder().id(10L).nome("Cliente").email(EMAIL).senha("x").ativo(ativo).build();
    }

    private PasswordResetToken token(LocalDateTime expiracao, LocalDateTime uso) {
        return PasswordResetToken.builder()
                .token("abc")
                .usuario(usuario(true))
                .dataExpiracao(expiracao)
                .dataUso(uso)
                .build();
    }


    @Test
    @DisplayName("e-mail não cadastrado não gera token nem revela a existência da conta")
    void emailInexistente() {
        when(usuarioService.buscarPorEmailOpcional(EMAIL)).thenReturn(Optional.empty());

        assertThat(passwordResetService.solicitar(EMAIL)).isEmpty();
        verifyNoInteractions(tokenRepository);
    }

    @Test
    @DisplayName("usuário inativo não recebe link")
    void usuarioInativo() {
        when(usuarioService.buscarPorEmailOpcional(EMAIL)).thenReturn(Optional.of(usuario(false)));

        assertThat(passwordResetService.solicitar(EMAIL)).isEmpty();
        verifyNoInteractions(tokenRepository);
    }

    @Test
    @DisplayName("gera token válido por 30 minutos e invalida os pendentes")
    void geraToken() {
        Usuario u = usuario(true);
        when(usuarioService.buscarPorEmailOpcional(EMAIL)).thenReturn(Optional.of(u));

        Optional<String> link = passwordResetService.solicitar(EMAIL);

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).invalidarPendentesDoUsuario(eq(10L), any(LocalDateTime.class));
        verify(tokenRepository).save(captor.capture());

        PasswordResetToken salvo = captor.getValue();
        assertThat(salvo.getUsuario()).isSameAs(u);
        assertThat(salvo.getToken()).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(salvo.getDataExpiracao())
                .isBetween(LocalDateTime.now().plusMinutes(29), LocalDateTime.now().plusMinutes(31));
        assertThat(link).contains("http://front/redefinir-senha?token=" + salvo.getToken());
    }

    @Test
    @DisplayName("tokens gerados são diferentes a cada solicitação")
    void tokensUnicos() {
        when(usuarioService.buscarPorEmailOpcional(EMAIL)).thenReturn(Optional.of(usuario(true)));

        String primeiro = passwordResetService.solicitar(EMAIL).orElseThrow();
        String segundo = passwordResetService.solicitar(EMAIL).orElseThrow();

        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    @DisplayName("com exporLink=false o token é salvo mas o link não é devolvido")
    void naoExpoeLink() {
        ReflectionTestUtils.setField(passwordResetService, "exporLink", false);
        when(usuarioService.buscarPorEmailOpcional(EMAIL)).thenReturn(Optional.of(usuario(true)));

        assertThat(passwordResetService.solicitar(EMAIL)).isEmpty();
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }


    @Test
    @DisplayName("redefine a senha e marca o token como usado")
    void redefineSenha() {
        PasswordResetToken t = token(LocalDateTime.now().plusMinutes(10), null);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));

        passwordResetService.redefinir(new RedefinirSenhaRequest("abc", "novaSenha123"));

        verify(usuarioService).definirSenha(t.getUsuario(), "novaSenha123");
        verify(tokenRepository).save(t);
        assertThat(t.usado()).isTrue();
    }

    @Test
    @DisplayName("token inexistente é rejeitado")
    void tokenInexistente() {
        when(tokenRepository.findByToken("xyz")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.redefinir(new RedefinirSenhaRequest("xyz", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inválido");
        verify(usuarioService, never()).definirSenha(any(), any());
    }

    @Test
    @DisplayName("token já usado é rejeitado")
    void tokenUsado() {
        when(tokenRepository.findByToken("abc"))
                .thenReturn(Optional.of(token(LocalDateTime.now().plusMinutes(10), LocalDateTime.now())));

        assertThatThrownBy(() -> passwordResetService.redefinir(new RedefinirSenhaRequest("abc", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi utilizado");
        verify(usuarioService, never()).definirSenha(any(), any());
    }

    @Test
    @DisplayName("token expirado é rejeitado")
    void tokenExpirado() {
        when(tokenRepository.findByToken("abc"))
                .thenReturn(Optional.of(token(LocalDateTime.now().minusMinutes(1), null)));

        assertThatThrownBy(() -> passwordResetService.redefinir(new RedefinirSenhaRequest("abc", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expirou");
        verify(usuarioService, never()).definirSenha(any(), any());
    }
}
