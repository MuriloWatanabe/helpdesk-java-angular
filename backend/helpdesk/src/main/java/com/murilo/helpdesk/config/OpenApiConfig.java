package com.murilo.helpdesk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura o botão "Authorize" do Swagger. Sem isso era preciso montar o
 * header Authorization manualmente para testar qualquer endpoint protegido.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    public OpenAPI helpdeskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helpdesk API")
                        .version("2.0")
                        .description("""
                                API do sistema de gestão de chamados.

                                Autentique-se em POST /v1/auth/login e informe o token
                                retornado no botão Authorize.

                                Usuários de demonstração (senha: 123456):
                                admin@helpdesk.com · tecnico@helpdesk.com · cliente@helpdesk.com
                                """)
                        .contact(new Contact().name("Murilo")))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Cole apenas o token, sem o prefixo Bearer.")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
