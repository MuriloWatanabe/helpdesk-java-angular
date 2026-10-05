package com.murilo.helpdesk.security;

import com.jayway.jsonpath.JsonPath;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Token JWT — fluxo de autenticação ponta a ponta")
class AuthTokenIntegrationTest {

    private static final String EMAIL = "token.teste@helpdesk.com";
    private static final String SENHA = "123456";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private MockMvc mvc;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuário Token")
                .email(EMAIL)
                .senha(passwordEncoder.encode(SENHA))
                .ativo(true)
                .perfis(new java.util.HashSet<>(Set.of(Perfil.CLIENTE.getCodigo())))
                .build());
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    private String login(String email) throws Exception {
        String corpo = mvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    @Test
    @DisplayName("token emitido no login dá acesso a /me")
    void login_tokenValido() throws Exception {
        String token = login(EMAIL);

        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    @DisplayName("sem token a API responde 401 em JSON")
    void semToken_401() throws Exception {
        mvc.perform(get("/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("token malformado responde 401 (e não 500)")
    void tokenMalformado_401() throws Exception {
        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido. Faça login novamente."));
    }

    @Test
    @DisplayName("token expirado responde 401 com mensagem de sessão expirada")
    void tokenExpirado_401() throws Exception {
        String expirado = Jwts.builder()
                .issuer("helpdesk-api")
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sua sessão expirou. Faça login novamente."));
    }

    @Test
    @DisplayName("token de usuário excluído responde 401 (e não 500)")
    void usuarioExcluido_401() throws Exception {
        String token = login(EMAIL);
        usuarioRepository.deleteAll();

        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("usuário desativado perde o acesso mesmo com token ainda válido")
    void usuarioDesativado_401() throws Exception {
        String token = login(EMAIL);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário inativo. Procure um administrador."));
    }

    @Test
    @DisplayName("trocar o próprio e-mail não invalida a sessão")
    void trocarEmail_mantemSessao() throws Exception {
        String token = login(EMAIL);
        String novoEmail = "novo.email@helpdesk.com";

        mvc.perform(put("/v1/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Usuário Token\",\"email\":\"%s\"}".formatted(novoEmail)))
                .andExpect(status().isOk());

        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(novoEmail));
    }
}
