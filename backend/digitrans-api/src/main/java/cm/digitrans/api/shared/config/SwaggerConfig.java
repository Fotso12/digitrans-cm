package cm.digitrans.api.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI digitransOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DIGITRANS-CM API")
                        .description("APIs REST — Projet DIGITRANS-CM — AGROCAM S.A.\n\n" +
                                "Modules autonomes simulés : Supply Chain | CRM | ERP | BI")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CAMTECH SOLUTIONS S.A.")
                                .email("dev@camtech.cm")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi supplyChainApi() {
        return GroupedOpenApi.builder()
                .group("1-supply-chain")
                .displayName("Supply Chain Tracker")
                .pathsToMatch("/api/supply-chain/**")
                .build();
    }

    @Bean
    public GroupedOpenApi crmApi() {
        return GroupedOpenApi.builder()
                .group("2-crm")
                .displayName("CRM - Services Clients")
                .pathsToMatch("/api/crm/**")
                .build();
    }

    @Bean
    public GroupedOpenApi erpApi() {
        return GroupedOpenApi.builder()
                .group("3-erp")
                .displayName("ERP - Opérations Industrielles")
                .pathsToMatch("/api/erp/**")
                .build();
    }

    @Bean
    public GroupedOpenApi biApi() {
        return GroupedOpenApi.builder()
                .group("4-bi")
                .displayName("BI Dashboard - Analytics")
                .pathsToMatch("/api/bi/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("0-authentication")
                .displayName("Gateway Authentification")
                .pathsToMatch("/api/auth/**")
                .build();
    }
}
