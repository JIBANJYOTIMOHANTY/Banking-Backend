package com.learning.Project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .servers(List.of(new Server().url("/api/v1").description("Base API URL")))
                                .info(new Info()
                                                .title("Bank Management API")
                                                .version("1.0.0")
                                                .description("This is a RESTful API documentation for the Bank Account Management System. "
                                                                +
                                                                "It allows managing client bank accounts, performing operations like deposit, withdrawal, and account deletion.")
                                                .contact(new Contact()
                                                                .name("API Support")
                                                                .email("support@bank.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
        }

        @Bean
        public OpenApiCustomizer stripPrefixCustomizer() {
                return openApi -> {
                        String prefix = "/api/v1";
                        if (openApi.getPaths() != null) {
                                Paths newPaths = new Paths();
                                openApi.getPaths().forEach((key, pathItem) -> {
                                        String newPath = key.startsWith(prefix) ? key.substring(prefix.length()) : key;
                                        if (newPath.isEmpty()) {
                                                newPath = "/";
                                        }
                                        newPaths.addPathItem(newPath, pathItem);
                                });
                                openApi.setPaths(newPaths);
                        }
                };
        }
}
