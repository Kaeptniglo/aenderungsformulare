package com.demo.formulare.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Formular "Vertragsänderung".
 * <p>
 * Beispiel für stark voneinander abhängige Felder: Die Änderungsart steuert, welche Abschnitte
 * sichtbar und Pflicht sind; die Sparte schränkt die wählbaren Tarife ein; der Tarif schränkt
 * die wählbaren Laufzeiten ein.
 */
public class Vertragsaenderung {

    public enum Aenderungsart {
        TARIFWECHSEL("Tarifwechsel"),
        VERTRAGSUEBERNAHME("Vertragsübernahme"),
        KUENDIGUNG("Kündigung");

        private final String bezeichnung;

        Aenderungsart(String bezeichnung) {
            this.bezeichnung = bezeichnung;
        }

        public String getBezeichnung() {
            return bezeichnung;
        }
    }

    public enum Sparte {
        STROM("Strom"),
        GAS("Gas"),
        WAERME("Wärme");

        private final String bezeichnung;

        Sparte(String bezeichnung) {
            this.bezeichnung = bezeichnung;
        }

        public String getBezeichnung() {
            return bezeichnung;
        }
    }

    public enum Tarif {
        STROM_FLEX("Strom Flex", Sparte.STROM, 1),
        STROM_BASIS("Strom Basis", Sparte.STROM, 12, 24),
        STROM_OEKO("Strom Öko", Sparte.STROM, 12, 24, 36),
        GAS_BASIS("Gas Basis", Sparte.GAS, 12, 24),
        GAS_OEKO("Gas Klima", Sparte.GAS, 12, 24, 36),
        WAERME_KOMFORT("Wärme Komfort", Sparte.WAERME, 24, 36);

        private final String bezeichnung;
        private final Sparte sparte;
        private final List<Integer> laufzeiten;

        Tarif(String bezeichnung, Sparte sparte, Integer... laufzeiten) {
            this.bezeichnung = bezeichnung;
            this.sparte = sparte;
            this.laufzeiten = List.of(laufzeiten);
        }

        public String getBezeichnung() {
            return bezeichnung;
        }

        public Sparte getSparte() {
            return sparte;
        }

        /** Erlaubte Laufzeiten in Monaten. */
        public List<Integer> getLaufzeiten() {
            return laufzeiten;
        }

        public static List<Tarif> fuer(Sparte sparte) {
            if (sparte == null) {
                return List.of();
            }
            return Arrays.stream(values()).filter(t -> t.sparte == sparte).toList();
        }
    }

    public enum Kuendigungsgrund {
        UMZUG("Umzug"),
        ANBIETERWECHSEL("Anbieterwechsel"),
        TODESFALL("Todesfall"),
        SONSTIGES("Sonstiges");

        private final String bezeichnung;

        Kuendigungsgrund(String bezeichnung) {
            this.bezeichnung = bezeichnung;
        }

        public String getBezeichnung() {
            return bezeichnung;
        }
    }

    public static final String VERTRAGSNUMMER_REGEX = "^V-\\d{8}$";
    public static final int MINDESTVORLAUF_TAGE = 14;
    public static final int MINDESTALTER_JAHRE = 18;

    @NotBlank(message = "Kundennummer ist erforderlich")
    @Pattern(regexp = Adressaenderung.KUNDENNUMMER_REGEX, message = "Format: K-123456")
    private String kundennummer;

    @NotBlank(message = "Vertragsnummer ist erforderlich")
    @Pattern(regexp = VERTRAGSNUMMER_REGEX, message = "Format: V-12345678")
    private String vertragsnummer;

    @NotNull(message = "Änderungsart ist erforderlich")
    private Aenderungsart aenderungsart;

    @NotNull(message = "Sparte ist erforderlich")
    private Sparte sparte;

    @NotNull(message = "Gewünschter Zeitpunkt ist erforderlich")
    private LocalDate gewuenschtZum;

    // nur bei Tarifwechsel
    private Tarif tarif;
    private Integer laufzeitMonate;

    // nur bei Vertragsübernahme
    @Size(max = 100, message = "Maximal 100 Zeichen")
    private String neuerVertragspartnerName;
    private LocalDate neuerVertragspartnerGeburtsdatum;

    // nur bei Kündigung
    private Kuendigungsgrund kuendigungsgrund;

    @Size(max = 500, message = "Maximal 500 Zeichen")
    private String bemerkung;

    // ---------------------------------------------------------------------
    // Feldübergreifende Regeln
    // ---------------------------------------------------------------------

    public boolean istTarifwechsel() {
        return aenderungsart == Aenderungsart.TARIFWECHSEL;
    }

    public boolean istVertragsuebernahme() {
        return aenderungsart == Aenderungsart.VERTRAGSUEBERNAHME;
    }

    public boolean istKuendigung() {
        return aenderungsart == Aenderungsart.KUENDIGUNG;
    }

    /** Frühestmöglicher Zeitpunkt: Monatserster nach Ablauf des Mindestvorlaufs. */
    public static LocalDate fruehesterZeitpunkt(LocalDate heute) {
        LocalDate fruehestens = heute.plusDays(MINDESTVORLAUF_TAGE);
        return fruehestens.getDayOfMonth() == 1 ? fruehestens : fruehestens.withDayOfMonth(1).plusMonths(1);
    }

    @AssertTrue(message = "Der gewünschte Zeitpunkt muss ein Monatserster sein")
    public boolean isZeitpunktMonatserster() {
        return gewuenschtZum == null || gewuenschtZum.getDayOfMonth() == 1;
    }

    @AssertTrue(message = "Der gewünschte Zeitpunkt muss mindestens " + MINDESTVORLAUF_TAGE + " Tage in der Zukunft liegen")
    public boolean isZeitpunktMitVorlauf() {
        return gewuenschtZum == null || !gewuenschtZum.isBefore(fruehesterZeitpunkt(LocalDate.now()));
    }

    @AssertTrue(message = "Bei Tarifwechsel müssen Tarif und Laufzeit gewählt werden")
    public boolean isTarifwechselVollstaendig() {
        return !istTarifwechsel() || (tarif != null && laufzeitMonate != null);
    }

    @AssertTrue(message = "Der Tarif passt nicht zur gewählten Sparte")
    public boolean isTarifPasstZurSparte() {
        return tarif == null || sparte == null || tarif.getSparte() == sparte;
    }

    @AssertTrue(message = "Die Laufzeit ist für den gewählten Tarif nicht verfügbar")
    public boolean isLaufzeitPasstZumTarif() {
        return tarif == null || laufzeitMonate == null || tarif.getLaufzeiten().contains(laufzeitMonate);
    }

    @AssertTrue(message = "Bei Vertragsübernahme müssen Name und Geburtsdatum des neuen Vertragspartners angegeben werden")
    public boolean isVertragsuebernahmeVollstaendig() {
        return !istVertragsuebernahme()
                || (neuerVertragspartnerName != null && !neuerVertragspartnerName.isBlank()
                && neuerVertragspartnerGeburtsdatum != null);
    }

    @AssertTrue(message = "Der neue Vertragspartner muss volljährig sein")
    public boolean isNeuerVertragspartnerVolljaehrig() {
        return neuerVertragspartnerGeburtsdatum == null
                || !neuerVertragspartnerGeburtsdatum.isAfter(LocalDate.now().minusYears(MINDESTALTER_JAHRE));
    }

    @AssertTrue(message = "Bei Kündigung muss ein Kündigungsgrund angegeben werden")
    public boolean isKuendigungsgrundAngegeben() {
        return !istKuendigung() || kuendigungsgrund != null;
    }

    @AssertTrue(message = "Bei Kündigungsgrund \"Sonstiges\" ist eine Bemerkung erforderlich")
    public boolean isBemerkungBeiSonstigemGrund() {
        return !(istKuendigung() && kuendigungsgrund == Kuendigungsgrund.SONSTIGES)
                || (bemerkung != null && !bemerkung.isBlank());
    }

    // --- Getter/Setter ---

    public String getKundennummer() {
        return kundennummer;
    }

    public void setKundennummer(String kundennummer) {
        this.kundennummer = kundennummer;
    }

    public String getVertragsnummer() {
        return vertragsnummer;
    }

    public void setVertragsnummer(String vertragsnummer) {
        this.vertragsnummer = vertragsnummer;
    }

    public Aenderungsart getAenderungsart() {
        return aenderungsart;
    }

    public void setAenderungsart(Aenderungsart aenderungsart) {
        this.aenderungsart = aenderungsart;
    }

    public Sparte getSparte() {
        return sparte;
    }

    public void setSparte(Sparte sparte) {
        this.sparte = sparte;
    }

    public LocalDate getGewuenschtZum() {
        return gewuenschtZum;
    }

    public void setGewuenschtZum(LocalDate gewuenschtZum) {
        this.gewuenschtZum = gewuenschtZum;
    }

    public Tarif getTarif() {
        return tarif;
    }

    public void setTarif(Tarif tarif) {
        this.tarif = tarif;
    }

    public Integer getLaufzeitMonate() {
        return laufzeitMonate;
    }

    public void setLaufzeitMonate(Integer laufzeitMonate) {
        this.laufzeitMonate = laufzeitMonate;
    }

    public String getNeuerVertragspartnerName() {
        return neuerVertragspartnerName;
    }

    public void setNeuerVertragspartnerName(String neuerVertragspartnerName) {
        this.neuerVertragspartnerName = neuerVertragspartnerName;
    }

    public LocalDate getNeuerVertragspartnerGeburtsdatum() {
        return neuerVertragspartnerGeburtsdatum;
    }

    public void setNeuerVertragspartnerGeburtsdatum(LocalDate neuerVertragspartnerGeburtsdatum) {
        this.neuerVertragspartnerGeburtsdatum = neuerVertragspartnerGeburtsdatum;
    }

    public Kuendigungsgrund getKuendigungsgrund() {
        return kuendigungsgrund;
    }

    public void setKuendigungsgrund(Kuendigungsgrund kuendigungsgrund) {
        this.kuendigungsgrund = kuendigungsgrund;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }
}
