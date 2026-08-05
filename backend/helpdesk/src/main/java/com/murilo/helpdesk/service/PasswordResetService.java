package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.RedefinirSenhaRequest;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.model.PasswordResetToken;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Fluxo "esqueci minha senha".
 *
 * O projeto não tem servidor de e-mail configurado, então o link é registrado
 * no log da aplicação. Quando {@code app.reset-senha.expor-link} estiver
 * habilitado (padrão em desenvolvimento), o link também volta na resposta para
 * que o fluxo possa ser testado ponta a ponta. Em produção, mantenha desligado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int VALIDADE_MINUTOS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioService usuarioService;

    @Value("${app.reset-senha.expor-link:true}")
    private boolean exporLink;

    @Value("${app.front-url:http://localhost:4200}")
    private String frontUrl;

    /**
     * Gera o link de redefinição. A resposta é sempre a mesma, exista ou não o
     * e-mail — caso contrário o endpoint viraria um verificador de cadastros.
     */
    @Transactional
    public Optional<String> solicitar(String email) {
        Optional<Usuario> encontrado = usuarioService.buscarPorEmailOpcional(email);
        if (encontrado.isEmpty()) {
            log.info("Recuperação solicitada para e-mail não cadastrado: {}", email);
            return Optional.empty();
        }

        Usuario usuario = encontrado.get();
        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            log.info("Recuperação solicitada para usuário inativo: {}", email);
            return Optional.empty();
        }

        tokenRepository.invalidarPendentesDoUsuario(usuario.getId(), LocalDateTime.now());

        String token = gerarToken();
        tokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .dataExpiracao(LocalDateTime.now().plusMinutes(VALIDADE_MINUTOS))
                .build());

        String link = frontUrl + "/redefinir-senha?token=" + token;
        log.info("Link de redefinição de senha para {}: {}", usuario.getEmail(), link);

        return exporLink ? Optional.of(link) : Optional.empty();
    }

    @Transactional
    public void redefinir(RedefinirSenhaRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException("Link inválido ou já utilizado."));

        if (token.usado()) {
            throw new BusinessException("Este link já foi utilizado. Solicite um novo.");
        }
        if (token.expirado()) {
            throw new BusinessException("Este link expirou. Solicite um novo.");
        }

        usuarioService.definirSenha(token.getUsuario(), request.novaSenha());

        token.setDataUso(LocalDateTime.now());
        tokenRepository.save(token);

        log.info("Senha redefinida para {}", token.getUsuario().getEmail());
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
