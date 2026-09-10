package com.demo.formulare.dokument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Echter REST-Client zum Dokumenterstellungs-System. Aktiv bei {@code dokumente.api.enabled=true}.
 * <p>
 * Erwartetes Protokoll (Annahme für den Prototyp, an das Zielsystem anzupassen):
 * {@code POST {baseUrl}{pfad}} mit {@link DokumentAnfrage} als JSON-Body, Antwort als {@link DokumentAntwort}.
 */
@Component
@ConditionalOnProperty(name = "dokumente.api.enabled", havingValue = "true")
public class RestDokumentClient implements DokumentErstellungClient {

    private static final Logger LOG = LoggerFactory.getLogger(RestDokumentClient.class);

    private final RestClient restClient;
    private final String pfad;

    public RestDokumentClient(RestClient.Builder builder, DokumenteApiProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
        this.pfad = properties.pfad();
        LOG.info("Dokumenterstellungs-System: {}{}", properties.baseUrl(), pfad);
    }

    @Override
    public DokumentAntwort erstelleDokument(DokumentAnfrage anfrage) {
        try {
            DokumentAntwort antwort = restClient.post()
                    .uri(pfad)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(anfrage)
                    .retrieve()
                    .body(DokumentAntwort.class);
            if (antwort == null) {
                throw new DokumentErstellungException("Leere Antwort vom Dokumenterstellungs-System", null);
            }
            return antwort;
        } catch (RestClientException e) {
            throw new DokumentErstellungException("Dokument konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }
}
