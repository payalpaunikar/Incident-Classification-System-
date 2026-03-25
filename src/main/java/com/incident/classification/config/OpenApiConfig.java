package com.incident.classification.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Incident Classification API")
                        .description("Api document for the Incident Classification application.")
                        .version("1.0.0"))
                .servers(List.of(new Server().url("http://localhost:8090").description("Local")));
    }
}
