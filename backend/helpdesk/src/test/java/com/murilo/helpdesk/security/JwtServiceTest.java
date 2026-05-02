package com.murilo.helpdesk.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService — testes unitários")
class JwtServiceTest {

    private static final String SECRET =
            "helpdesk-secret-key-2024-muito-longa-para-hmac-sha256-minimo-256bits";
    private static final long EXPIRATION = 86_400_000L; // 24 h

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    private UserDetails user(String username) {
        return User.withUsername(username)
                .password("password")
                .roles("USER")
                .build();
    }

    // ──────────────────────────────────────────────────────────
    // generateToken
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken — deve retornar token JWT não vazio")
    void generateToken_retornaTokenNaoVazio() {
        var token = jwtService.generateToken(user("user@test.com"));

        assertThat(token).isNotBlank();
        // JWT tem exatamente 3 partes separadas por ponto
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ──────────────────────────────────────────────────────────
    // extractUsername
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername — deve extrair o e-mail correto do token")
    void extractUsername_retornaUsernameCorreto() {
        var userDetails = user("user@test.com");
        var token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@test.com");
    }

    // ──────────────────────────────────────────────────────────
    // isTokenValid
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid — token do próprio usuário deve ser válido")
    void isTokenValid_comTokenCorreto_retornaVerdadeiro() {
        var userDetails = user("user@test.com");
        var token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid — token de outro usuário deve ser inválido")
    void isTokenValid_comOutroUsuario_retornaFalso() {
        var originalUser = user("user@test.com");
        var otherUser = user("other@test.com");
        var token = jwtService.generateToken(originalUser);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }
}
