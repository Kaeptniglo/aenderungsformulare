package com.demo.formulare.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Einfacher Erreichbarkeits-Check für Hosting-Dienste (Render, Fly, Load Balancer).
 * Bleibt auch bei aktivem Zugangsschutz ohne Anmeldung erreichbar.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
