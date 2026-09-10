package com.demo.formulare.formular;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.demo.formulare.dokument.DokumentAnfrage;
import com.demo.formulare.dokument.DokumentAntwort;
import com.demo.formulare.dokument.DokumentErstellungClient;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Zentrale Backend-Logik: nimmt ein ausgefülltes Formular entgegen, validiert es serverseitig,
 * wandelt es in Nutzdaten für das Dokumenterstellungs-System um und legt die Einreichung ab.
 */
@Service
public class FormularService {

    private static final Logger LOG = LoggerFactory.getLogger(FormularService.class);

    private final Validator validator;
    private final ObjectMapper nutzdatenMapper;
    private final DokumentErstellungClient dokumentClient;
    private final EinreichungRepository repository;

    public FormularService(Validator validator,
                           ObjectMapper objectMapper,
                           DokumentErstellungClient dokumentClient,
                           EinreichungRepository repository) {
        this.validator = validator;
        // Nur die Felder serialisieren, nicht die Regel-Methoden (isXyz()) der Modellklassen
        this.nutzdatenMapper = objectMapper.rebuild()
                .changeDefaultVisibility(vc -> vc
                        .withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY))
                .build();
        this.dokumentClient = dokumentClient;
        this.repository = repository;
    }

    /**
     * Reicht ein Formular ein.
     *
     * @param typ   Formulartyp
     * @param daten ausgefülltes Formularmodell (Instanz von {@link FormularTyp#getDatenKlasse()})
     * @return die gespeicherte Einreichung inkl. Antwort des Dokumenterstellungs-Systems
     * @throws FormularValidierungException wenn die serverseitige Validierung fehlschlägt
     */
    public FormularEinreichung einreichen(FormularTyp typ, Object daten) {
        if (!typ.getDatenKlasse().isInstance(daten)) {
            throw new IllegalArgumentException("Daten vom Typ " + daten.getClass().getSimpleName()
                    + " passen nicht zum Formular " + typ);
        }
        validieren(daten);

        Map<String, Object> nutzdaten = nutzdatenMapper.convertValue(daten, new TypeReference<>() {
        });
        DokumentAnfrage anfrage = new DokumentAnfrage(typ.getVorlagenId(), typ.getTitel(), nutzdaten);
        DokumentAntwort antwort = dokumentClient.erstelleDokument(anfrage);

        FormularEinreichung einreichung = new FormularEinreichung(
                UUID.randomUUID().toString(), typ, Instant.now(), nutzdaten, antwort);
        repository.speichern(einreichung);
        LOG.info("Formular {} eingereicht, Einreichung {}, Dokument {}", typ, einreichung.id(), antwort.dokumentId());
        return einreichung;
    }

    public List<FormularEinreichung> alleEinreichungen() {
        return repository.alle();
    }

    public FormularEinreichung findeEinreichung(String id) {
        return repository.finde(id)
                .orElseThrow(() -> new IllegalArgumentException("Einreichung nicht gefunden: " + id));
    }

    private void validieren(Object daten) {
        Set<ConstraintViolation<Object>> verletzungen = validator.validate(daten);
        if (!verletzungen.isEmpty()) {
            List<String> fehler = verletzungen.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .toList();
            throw new FormularValidierungException(fehler);
        }
    }
}
