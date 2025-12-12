package com.cantinho_emocoes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // --- CORREÇÃO: REMOVIDO O CONFLITO DE CORS ---
    // O método addCorsMappings foi removido daqui porque ele conflitava 
    // com o SecurityConfig.java, bloqueando o site.
    // Agora o SecurityConfig gerencia tudo sozinho.

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuração para servir imagens salvas na pasta uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}