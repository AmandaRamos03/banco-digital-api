package com.amanda.bancodigital.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class LinksIniciais {

    private static final Logger log = LoggerFactory.getLogger(LinksIniciais.class);

    private final Environment environment;

    public LinksIniciais(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void mostrarLinks() {
        String base = "http://localhost:" + environment.getProperty("local.server.port", "8080");
        log.info("""

                ----------------------------------------------------------
                  Banco Digital API rodando!
                  API:     {}
                  Swagger: {}/swagger-ui.html
                ----------------------------------------------------------""", base, base);
    }
}
