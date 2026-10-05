package com.murilo.helpdesk.security;

import com.murilo.helpdesk.model.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DisplayName("JwtService — testes unitários")
class JwtServiceTest {

    private static final String SECRET =
            "helpdesk-secret-key-2024-muito-longa-para-hmac-sha256-minimo-256bits";
    private static final long EXPIRATION = 86_400_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = criar(SECRET, EXPIRATION);
    }

    private JwtService criar(String secret, long expiration) {
        var service = new JwtService();
        ReflectionTestUtils.setField(service, "jwtSecret", secret);
        ReflectionTestUtils.setField(service, "jwtExpiration", expiration);
        service.init();
        return service;
    }

    private Usuario usuario(Long id, String email) {
        return Usuario.builder().id(id).email(email).nome("Teste").senha("x").build();
    }


    @Test
    @DisplayName("generateToken — deve retornar token JWT não vazio")
    void generateToken_retornaTokenNaoVazio() {
        var token = jwtService.generateToken(usuario(1L, "user@test.com"));

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUserId — o subject do token é o id do usuário")
    void extractUserId_retornaIdDoUsuario() {
        var token = jwtService.generateToken(usuario(42L, "user@test.com"));

        assertThat(jwtService.extractUserId(jwtService.parseToken(token))).isEqualTo(42L);
    }

    @Test
    @DisplayName("extractExpiration — respeita o tempo configurado em jwt.expiration")
    void extractExpiration_respeitaConfiguracao() {
        var token = jwtService.generateToken(usuario(1L, "user@test.com"));

        var esperado = Instant.now().plusMillis(EXPIRATION);
        assertThat(jwtService.extractExpiration(token))
                .isCloseTo(esperado, within(5, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("parseToken — token expirado lança ExpiredJwtException")
    void parseToken_tokenExpirado() {
        var token = Jwts.builder()
                .issuer("helpdesk-api")
                .subject("1")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtService.parseToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("parseToken — token assinado com outra chave é recusado")
    void parseToken_assinaturaInvalida() {
        var outraChave = criar(SECRET + "-diferente", EXPIRATION);
        var token = outraChave.generateToken(usuario(1L, "user@test.com"));

        assertThatThrownBy(() -> jwtService.parseToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("parseToken — texto que não é JWT é recusado")
    void parseToken_malformado() {
        assertThatThrownBy(() -> jwtService.parseToken("isto-nao-e-um-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("init — chave curta demais impede a aplicação de subir")
    void init_chaveCurta() {
        assertThatThrownBy(() -> criar("curta", EXPIRATION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret");
    }
}
