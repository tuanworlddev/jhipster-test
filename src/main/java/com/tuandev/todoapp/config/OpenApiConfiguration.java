package com.tuandev.todoapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class OpenApiConfiguration {

    @Bean
    public OpenAPI todoAppOpenAPI(
        @Value("${jhipster.api-docs.title:Todo App API}") String title,
        @Value("${jhipster.api-docs.description:Todo App API documentation}") String description,
        @Value("${jhipster.api-docs.version:0.0.1}") String version,
        @Value("${jhipster.api-docs.terms-of-service-url:}") String termsOfServiceUrl,
        @Value("${jhipster.api-docs.contact-name:}") String contactName,
        @Value("${jhipster.api-docs.contact-url:}") String contactUrl,
        @Value("${jhipster.api-docs.contact-email:}") String contactEmail,
        @Value("${jhipster.api-docs.license-name:}") String licenseName,
        @Value("${jhipster.api-docs.license-url:}") String licenseUrl
    ) {
        Contact contact = new Contact();
        if (!contactName.isEmpty()) {
            contact.name(contactName);
        }
        if (!contactUrl.isEmpty()) {
            contact.url(contactUrl);
        }
        if (!contactEmail.isEmpty()) {
            contact.email(contactEmail);
        }

        License license = new License();
        if (!licenseName.isEmpty()) {
            license.name(licenseName);
        }
        if (!licenseUrl.isEmpty()) {
            license.url(licenseUrl);
        }

        Info info = new Info().title(title).description(description).version(version).contact(contact).license(license);

        if (!termsOfServiceUrl.isEmpty()) {
            info.termsOfService(termsOfServiceUrl);
        }

        return new OpenAPI()
            .info(info)
            .addServersItem(new Server().url("/"))
            .components(
                new Components().addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
