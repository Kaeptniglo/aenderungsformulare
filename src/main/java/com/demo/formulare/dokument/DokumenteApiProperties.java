package com.demo.formulare.dokument;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfiguration der Anbindung an das Dokumenterstellungs-System (Präfix {@code dokumente.api}).
 *
 * @param enabled {@code true}: echter REST-Aufruf, {@code false}: simulierter Client
 * @param baseUrl Basis-URL des Systems, z. B. {@code http://localhost:9000}
 * @param pfad    Pfad des Endpunkts, z. B. {@code /api/dokumente}
 */
@ConfigurationProperties(prefix = "dokumente.api")
public record DokumenteApiProperties(boolean enabled, String baseUrl, String pfad) {
}
