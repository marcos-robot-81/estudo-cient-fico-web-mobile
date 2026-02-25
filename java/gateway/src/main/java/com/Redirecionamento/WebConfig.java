package com.Redirecionamento; // Ajustado para sua pasta atual

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapeia a raiz (/**) para buscar arquivos.
        registry.addResourceHandler("/**")
                // 1ª Prioridade: Pasta no Celular (permite atualização via SCP)
                .addResourceLocations("file:/data/data/com.termux/files/home/meu-front/dist/meu-front/browser/")
                // 2ª Prioridade: Pasta dentro do JAR (backup se o SCP falhar)
                .addResourceLocations("classpath:/static/");  // Esse e o que fuciona.
    }
}