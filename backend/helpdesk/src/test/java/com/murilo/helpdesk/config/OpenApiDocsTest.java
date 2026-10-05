package com.murilo.helpdesk.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Swagger / OpenAPI — documentação pública da API")
class OpenApiDocsTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("/v3/api-docs é acessível sem token e expõe o esquema bearer")
    void apiDocs_publico() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("/api"))
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/v1/chamados'].get").exists());
    }

    @Test
    @DisplayName("endpoints públicos de autenticação não exigem token no Swagger")
    void login_semCadeado() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/v1/auth/login'].post.security", empty()))
                .andExpect(jsonPath("$.paths['/v1/auth/register'].post.security", empty()))
                .andExpect(jsonPath("$.paths['/v1/auth/me'].get.security").doesNotExist());
    }

    @Test
    @DisplayName("paginação aparece como page/size/sort e não como objeto JSON")
    void pageable_expandido() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/v1/chamados'].get.parameters[*].name", hasItem("page")))
                .andExpect(jsonPath("$.paths['/v1/chamados'].get.parameters[*].name", hasItem("size")))
                .andExpect(jsonPath("$.paths['/v1/chamados'].get.parameters[*].name", hasItem("sort")))
                .andExpect(jsonPath("$.paths['/v1/chamados'].get.parameters[*].name", not(hasItem("pageable"))));
    }

    @Test
    @DisplayName("Swagger UI e sua configuração carregam sem autenticação")
    void swaggerUi_publico() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui")));

        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persistAuthorization").value(true));
    }
}
