package com.demo.formulare.formular;

import com.demo.formulare.model.Adressaenderung;
import com.demo.formulare.model.Bankverbindungsaenderung;
import com.demo.formulare.model.Vertragsaenderung;

import java.util.Arrays;
import java.util.Optional;

/**
 * Katalog aller verfügbaren Formulare. Neue Formulare werden hier registriert
 * (plus Modellklasse im Paket {@code model} und View im Paket {@code ui}).
 */
public enum FormularTyp {

    ADRESSAENDERUNG(
            "adressaenderung",
            "Adressänderung",
            "Neue Wohnanschrift und optional eine abweichende Postanschrift melden.",
            "VORLAGE_ADRESSAENDERUNG_V1",
            Adressaenderung.class),

    BANKVERBINDUNG(
            "bankverbindung",
            "Änderung der Bankverbindung",
            "Neue IBAN/BIC hinterlegen und SEPA-Lastschriftmandat erteilen.",
            "VORLAGE_BANKVERBINDUNG_V1",
            Bankverbindungsaenderung.class),

    VERTRAGSAENDERUNG(
            "vertragsaenderung",
            "Vertragsänderung",
            "Tarifwechsel, Vertragsübernahme oder Kündigung eines bestehenden Vertrags.",
            "VORLAGE_VERTRAGSAENDERUNG_V1",
            Vertragsaenderung.class);

    public static final String ROUTE_PRAEFIX = "formulare/";

    private final String id;
    private final String titel;
    private final String beschreibung;
    private final String vorlagenId;
    private final Class<?> datenKlasse;

    FormularTyp(String id, String titel, String beschreibung, String vorlagenId, Class<?> datenKlasse) {
        this.id = id;
        this.titel = titel;
        this.beschreibung = beschreibung;
        this.vorlagenId = vorlagenId;
        this.datenKlasse = datenKlasse;
    }

    /** Technischer Schlüssel, z. B. für REST-Pfade und Routen. */
    public String getId() {
        return id;
    }

    public String getTitel() {
        return titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    /** Kennung der Vorlage im Dokumenterstellungs-System. */
    public String getVorlagenId() {
        return vorlagenId;
    }

    /** Modellklasse, die die Formulardaten trägt (inkl. Bean-Validation-Regeln). */
    public Class<?> getDatenKlasse() {
        return datenKlasse;
    }

    /** Vaadin-Route der zugehörigen View. */
    public String getRoute() {
        return ROUTE_PRAEFIX + id;
    }

    public static Optional<FormularTyp> vonId(String id) {
        return Arrays.stream(values()).filter(t -> t.id.equalsIgnoreCase(id)).findFirst();
    }
}
