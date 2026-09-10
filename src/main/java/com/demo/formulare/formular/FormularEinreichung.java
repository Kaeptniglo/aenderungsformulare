package com.demo.formulare.formular;

import com.demo.formulare.dokument.DokumentAntwort;

import java.time.Instant;
import java.util.Map;

/**
 * Ein eingereichtes Formular inklusive der Nutzdaten, die an das Dokumenterstellungs-System
 * geschickt wurden, und dessen Antwort.
 */
public record FormularEinreichung(
        String id,
        FormularTyp typ,
        Instant zeitpunkt,
        Map<String, Object> daten,
        DokumentAntwort dokument) {
}
