package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.repository.PasswordResetTokenRepository;
import com.murilo.helpdesk.repository.UsuarioRepository;
import com.murilo.helpdesk.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Gestão de usuários — regras ponta a ponta")
class UsuarioGestaoIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private MockMvc mvc;
    private Usuario admin;
    private Usuario outroAdmin;
    private Usuario cliente;
    private String tokenAdmin;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        admin = salvar("Admin Principal", "admin.gestao@helpdesk.com", Perfil.ADMIN);
        outroAdmin = salvar("Admin Reserva", "reserva.gestao@helpdesk.com", Perfil.ADMIN);
        cliente = salvar("Cliente Gestão", "cliente.gestao@helpdesk.com", Perfil.CLIENTE);
        tokenAdmin = "Bearer " + jwtService.generateToken(admin);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private Usuario salvar(String nome, String email, Perfil perfil) {
        return usuarioRepository.save(Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .ativo(true)
                .perfis(new HashSet<>(Set.of(perfil.getCodigo())))
                .build());
    }

    private String corpoUsuario(String email, String senha, boolean ativo, int... perfis) {
        String senhaJson = senha == null ? "null" : "\"" + senha + "\"";
        String perfisJson = java.util.Arrays.stream(perfis)
                .mapToObj(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        return """
                {"nome":"Usuário Teste","email":"%s","senha":%s,"ativo":%s,"perfis":[%s]}"""
                .formatted(email, senhaJson, ativo, perfisJson);
    }

    @Test
    @DisplayName("admin não desativa a própria conta pelo formulário de edição")
    void naoDesativaPropriaContaPeloPut() throws Exception {
        mvc.perform(put("/v1/usuarios/{id}", admin.getId())
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoUsuario(admin.getEmail(), null, false, Perfil.ADMIN.getCodigo())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Você não pode desativar a própria conta."));

        assertThat(usuarioRepository.findById(admin.getId()).orElseThrow().getAtivo()).isTrue();
    }

    @Test
    @DisplayName("admin não remove o próprio perfil de administrador pelo formulário de edição")
    void naoRemovePropriaAdminPeloPut() throws Exception {
        mvc.perform(put("/v1/usuarios/{id}", admin.getId())
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoUsuario(admin.getEmail(), null, true, Perfil.CLIENTE.getCodigo())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Você não pode remover o próprio perfil de administrador."));
    }

    @Test
    @DisplayName("admin não remove o próprio perfil de administrador pelo PATCH de perfis")
    void naoRemovePropriaAdminPeloPatch() throws Exception {
        mvc.perform(patch("/v1/usuarios/{id}/perfis", admin.getId())
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + Perfil.TECNICO.getCodigo() + "]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("admin continua podendo rebaixar e desativar outro usuário")
    void editaOutroUsuario() throws Exception {
        mvc.perform(put("/v1/usuarios/{id}", outroAdmin.getId())
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoUsuario(outroAdmin.getEmail(), null, false, Perfil.TECNICO.getCodigo())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.perfilPrincipal").value("Técnico"));
    }

    @Test
    @DisplayName("senha acima do limite do BCrypt é erro de validação (400), não erro interno (500)")
    void senhaLongaDemais() throws Exception {
        mvc.perform(post("/v1/usuarios")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoUsuario("novo.gestao@helpdesk.com", "a".repeat(73), true,
                                Perfil.CLIENTE.getCodigo())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("e-mail maior que a coluna do banco é erro de validação (400), não conflito genérico")
    void emailLongoDemais() throws Exception {
        String email = "a".repeat(64) + "@" + "b".repeat(36) + ".com";

        mvc.perform(post("/v1/usuarios")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoUsuario(email, "123456", true, Perfil.CLIENTE.getCodigo())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("email"));
    }

    @Test
    @DisplayName("listagem e diretório saem ordenados por nome sem diferenciar maiúsculas; diretório omite inativos")
    void listagemEDiretorioOrdenados() throws Exception {
        salvar("beatriz minúscula", "beatriz.gestao@helpdesk.com", Perfil.CLIENTE);
        Usuario inativo = salvar("Bruno Inativo", "bruno.gestao@helpdesk.com", Perfil.CLIENTE);
        inativo.setAtivo(false);
        usuarioRepository.save(inativo);

        mvc.perform(get("/v1/usuarios").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(org.hamcrest.Matchers.contains(
                        "Admin Principal", "Admin Reserva", "beatriz minúscula",
                        "Bruno Inativo", "Cliente Gestão")));

        mvc.perform(get("/v1/usuarios/diretorio").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(org.hamcrest.Matchers.contains(
                        "Admin Principal", "Admin Reserva", "beatriz minúscula", "Cliente Gestão")));
    }

    @Test
    @DisplayName("recuperar senha não devolve o link de redefinição por padrão")
    void recuperarSenhaNaoExpoeLink() throws Exception {
        mvc.perform(post("/v1/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(cliente.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalhe").doesNotExist());
    }
}
