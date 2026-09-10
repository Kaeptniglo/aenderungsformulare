package com.demo.formulare.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Formular "Änderung der Bankverbindung".
 * <p>
 * Beispiel für gegenseitige Beeinflussung: Die BIC ist nur Pflicht, wenn die IBAN nicht aus
 * Deutschland stammt. Bei abweichendem Kontoinhaber muss die Beziehung zum Kunden angegeben werden.
 */
public class Bankverbindungsaenderung {

    public enum Beziehung {
        EHEPARTNER("Ehepartner/in"),
        ELTERNTEIL("Elternteil"),
        KIND("Kind"),
        FIRMA("Firma / Arbeitgeber"),
        SONSTIGE("Sonstige");

        private final String bezeichnung;

        Beziehung(String bezeichnung) {
            this.bezeichnung = bezeichnung;
        }

        public String getBezeichnung() {
            return bezeichnung;
        }
    }

    public static final String BIC_REGEX = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$";

    @NotBlank(message = "Kundennummer ist erforderlich")
    @Pattern(regexp = Adressaenderung.KUNDENNUMMER_REGEX, message = "Format: K-123456")
    private String kundennummer;

    @NotBlank(message = "Kontoinhaber ist erforderlich")
    @Size(max = 100, message = "Maximal 100 Zeichen")
    private String kontoinhaber;

    @NotBlank(message = "IBAN ist erforderlich")
    private String iban;

    private String bic;

    @Size(max = 100, message = "Maximal 100 Zeichen")
    private String bankname;

    @NotNull(message = "Datum ist erforderlich")
    @FutureOrPresent(message = "Darf nicht in der Vergangenheit liegen")
    private LocalDate gueltigAb;

    private boolean abweichenderKontoinhaber;

    private Beziehung beziehungZumKunden;

    @AssertTrue(message = "Das SEPA-Lastschriftmandat muss erteilt werden")
    private boolean sepaMandatErteilt;

    // ---------------------------------------------------------------------
    // Feldübergreifende Regeln
    // ---------------------------------------------------------------------

    /** BIC ist Pflicht, sobald die IBAN nicht aus Deutschland stammt. */
    public boolean isBicErforderlich() {
        String code = IbanPruefer.laenderCode(iban);
        return code != null && !"DE".equals(code);
    }

    @AssertTrue(message = "IBAN ist ungültig")
    public boolean isIbanGueltig() {
        return iban == null || iban.isBlank() || IbanPruefer.istGueltig(iban);
    }

    @AssertTrue(message = "BIC ist bei ausländischer IBAN erforderlich")
    public boolean isBicVorhandenWennErforderlich() {
        return !isBicErforderlich() || (bic != null && !bic.isBlank());
    }

    @AssertTrue(message = "BIC hat kein gültiges Format (8 oder 11 Zeichen)")
    public boolean isBicFormatGueltig() {
        return bic == null || bic.isBlank() || bic.matches(BIC_REGEX);
    }

    @AssertTrue(message = "Bei abweichendem Kontoinhaber muss die Beziehung zum Kunden angegeben werden")
    public boolean isBeziehungAngegebenWennAbweichend() {
        return !abweichenderKontoinhaber || beziehungZumKunden != null;
    }

    // --- Getter/Setter ---

    public String getKundennummer() {
        return kundennummer;
    }

    public void setKundennummer(String kundennummer) {
        this.kundennummer = kundennummer;
    }

    public String getKontoinhaber() {
        return kontoinhaber;
    }

    public void setKontoinhaber(String kontoinhaber) {
        this.kontoinhaber = kontoinhaber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public LocalDate getGueltigAb() {
        return gueltigAb;
    }

    public void setGueltigAb(LocalDate gueltigAb) {
        this.gueltigAb = gueltigAb;
    }

    public boolean isAbweichenderKontoinhaber() {
        return abweichenderKontoinhaber;
    }

    public void setAbweichenderKontoinhaber(boolean abweichenderKontoinhaber) {
        this.abweichenderKontoinhaber = abweichenderKontoinhaber;
    }

    public Beziehung getBeziehungZumKunden() {
        return beziehungZumKunden;
    }

    public void setBeziehungZumKunden(Beziehung beziehungZumKunden) {
        this.beziehungZumKunden = beziehungZumKunden;
    }

    public boolean isSepaMandatErteilt() {
        return sepaMandatErteilt;
    }

    public void setSepaMandatErteilt(boolean sepaMandatErteilt) {
        this.sepaMandatErteilt = sepaMandatErteilt;
    }
}
