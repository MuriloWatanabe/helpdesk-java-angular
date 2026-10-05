package com.murilo.helpdesk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";


    // URL relativa: funciona tanto acessando a API direto (:9090/api) quanto pelo proxy do nginx (:4200/api).
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI helpdeskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helpdesk API")
                        .version("2.0")
                        .description("""
                                API do sistema de gestão de chamados.

                                Como autenticar:
                                1. Execute POST /v1/auth/login com e-mail e senha.
                                2. Copie o campo `token` da resposta.
                                3. Clique em **Authorize** e cole apenas o token (sem o prefixo Bearer).

                                Usuários de demonstração (senha: 123456):
                                admin@helpdesk.com · tecnico@helpdesk.com · cliente@helpdesk.com
                                """)
                        .contact(new Contact().name("Murilo")))
                .addServersItem(new Server()
                        .url(StringUtils.hasText(contextPath) ? contextPath : "/")
                        .description("Servidor atual"))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Cole apenas o token, sem o prefixo Bearer.")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
