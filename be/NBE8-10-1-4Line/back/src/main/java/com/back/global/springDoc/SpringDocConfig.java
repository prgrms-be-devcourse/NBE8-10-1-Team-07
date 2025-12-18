package com.back.global.springDoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "API 서버", version = "beta", description = "API 서버 문서입니다."))
public class SpringDocConfig {
    @Bean
    public GroupedOpenApi groupApiOrder() {
        return GroupedOpenApi.builder()
                .group("OrderAPI")
                .pathsToMatch("/api/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupApiCustomer() {
        return GroupedOpenApi.builder()
                .group("CustomerAPI")
                .pathsToMatch("/api/customers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupApiProduct() {
        return GroupedOpenApi.builder()
                .group("ProductAPI")
                .pathsToMatch("/api/products/**")
                .build();
    }
}