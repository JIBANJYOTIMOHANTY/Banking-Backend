package com.learning.Project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .servers(List.of(new Server().url("/api/v1").description("Base API URL")))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                                .components(new Components().addSecuritySchemes("Bearer Authentication",
                                                createAPIKeyScheme()))
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

        private SecurityScheme createAPIKeyScheme() {
                return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .bearerFormat("JWT")
                                .scheme("bearer");
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

        @Bean
        public SwaggerIndexTransformer swaggerIndexTransformer(
                        SwaggerUiConfigProperties swaggerUiConfigProperties,
                        SwaggerUiOAuthProperties swaggerUiOAuthProperties,
                        SwaggerWelcomeCommon swaggerWelcomeCommon,
                        ObjectMapperProvider objectMapperProvider) {
                return new SwaggerIndexPageTransformer(
                                swaggerUiConfigProperties,
                                swaggerUiOAuthProperties,
                                swaggerWelcomeCommon,
                                objectMapperProvider) {
                        @Override
                        public Resource transform(HttpServletRequest request, Resource resource,
                                        ResourceTransformerChain transformerChain) throws IOException {
                                Resource transformed = super.transform(request, resource, transformerChain);
                                String filename = resource.getFilename();
                                if (filename != null) {
                                        if (filename.equals("index.html")) {
                                                String html = new String(transformed.getInputStream().readAllBytes(),
                                                                StandardCharsets.UTF_8);
                                                String js = "\n<script>\n" +
                                                                "(function() {\n" +
                                                                "    const originalFetch = window.fetch;\n" +
                                                                "    window.fetch = function(...args) {\n" +
                                                                "        return originalFetch.apply(this, args).then(response => {\n"
                                                                +
                                                                "            const url = args[0];\n" +
                                                                "            if (typeof url === 'string' && (url.includes('/api/v1/auth/login') || url.includes('/api/v1/auth/refresh'))) {\n"
                                                                +
                                                                "                const clone = response.clone();\n" +
                                                                "                clone.json().then(data => {\n" +
                                                                "                    if (data && data.status === 0 && data.data && data.data.length > 0) {\n"
                                                                +
                                                                "                        const token = data.data[0].token;\n"
                                                                +
                                                                "                        if (token && window.ui) {\n" +
                                                                "                            window.ui.preauthorizeApiKey(\"Bearer Authentication\", token);\n"
                                                                +
                                                                "                            console.log(\"Automatically authorized token in Swagger UI.\");\n"
                                                                +
                                                                "                        }\n" +
                                                                "                    }\n" +
                                                                "                }).catch(err => console.error(\"Error reading login response:\", err));\n"
                                                                +
                                                                "            }\n" +
                                                                "            return response;\n" +
                                                                "        });\n" +
                                                                "    };\n" +
                                                                "})();\n" +
                                                                "</script>\n";
                                                html = html.replace("</body>", js + "</body>");
                                                return new TransformedResource(resource,
                                                                html.getBytes(StandardCharsets.UTF_8));
                                        } else if (filename.equals("swagger-initializer.js")) {
                                                String jsContent = new String(
                                                                transformed.getInputStream().readAllBytes(),
                                                                StandardCharsets.UTF_8);
                                                String js = "\n" +
                                                                "(function() {\n" +
                                                                "    const originalFetch = window.fetch;\n" +
                                                                "    window.fetch = function(...args) {\n" +
                                                                "        return originalFetch.apply(this, args).then(response => {\n"
                                                                +
                                                                "            const url = args[0];\n" +
                                                                "            if (typeof url === 'string' && (url.includes('/api/v1/auth/login') || url.includes('/api/v1/auth/refresh'))) {\n"
                                                                +
                                                                "                const clone = response.clone();\n" +
                                                                "                clone.json().then(data => {\n" +
                                                                "                    if (data && data.status === 0 && data.data && data.data.length > 0) {\n"
                                                                +
                                                                "                        const token = data.data[0].token;\n"
                                                                +
                                                                "                        if (token && window.ui) {\n" +
                                                                "                            window.ui.preauthorizeApiKey(\"Bearer Authentication\", token);\n"
                                                                +
                                                                "                            console.log(\"Automatically authorized token in Swagger UI.\");\n"
                                                                +
                                                                "                        }\n" +
                                                                "                    }\n" +
                                                                "                }).catch(err => console.error(\"Error reading login response:\", err));\n"
                                                                +
                                                                "            }\n" +
                                                                "            return response;\n" +
                                                                "        });\n" +
                                                                "    };\n" +
                                                                "})();\n";
                                                jsContent = jsContent + js;
                                                return new TransformedResource(resource,
                                                                jsContent.getBytes(StandardCharsets.UTF_8));
                                        }
                                }
                                return transformed;
                        }
                };
        }
}
