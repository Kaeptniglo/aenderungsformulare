package com.demo.formulare.dokument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Simuliert das Dokumenterstellungs-System: loggt die Nutzdaten, die gesendet würden,
 * und liefert eine erfundene Dokument-ID zurück. Aktiv, solange {@code dokumente.api.enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "dokumente.api.enabled", havingValue = "false", matchIfMissing = true)
public class SimulierterDokumentClient implements DokumentErstellungClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimulierterDokumentClient.class);
    private static final DateTimeFormatter ZEITSTEMPEL = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ObjectMapper objectMapper;

    public SimulierterDokumentClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public DokumentAntwort erstelleDokument(DokumentAnfrage anfrage) {
        try {
            LOG.info("[SIMULATION] Dokumentanfrage an Dokumenterstellungs-System:\n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage));
        } catch (JacksonException e) {
            LOG.warn("[SIMULATION] Anfrage konnte nicht serialisiert werden", e);
        }
        String dokumentId = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String dateiname = anfrage.vorlagenId() + "-" + LocalDateTime.now().format(ZEITSTEMPEL) + ".pdf";
        return new DokumentAntwort(dokumentId, dateiname, "SIMULIERT");
    }
}
