package com.demo.formulare.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Formular "Adressänderung".
 * <p>
 * Feldregeln stehen als Bean-Validation-Annotationen direkt am Feld. Sie werden sowohl im UI
 * (über {@code BeanValidationBinder}) als auch im Backend (jakarta {@code Validator}) geprüft.
 * Feldübergreifende Regeln sind als {@code @AssertTrue}-Methoden formuliert, damit UI und Backend
 * dieselbe Logik verwenden.
 */
public class Adressaenderung {

    public static final String KUNDENNUMMER_REGEX = "^K-\\d{6}$";

    @NotBlank(message = "Kundennummer ist erforderlich")
    @Pattern(regexp = KUNDENNUMMER_REGEX, message = "Format: K-123456")
    private String kundennummer;

    @NotBlank(message = "Vorname ist erforderlich")
    @Size(max = 50, message = "Maximal 50 Zeichen")
    private String vorname;

    @NotBlank(message = "Nachname ist erforderlich")
    @Size(max = 50, message = "Maximal 50 Zeichen")
    private String nachname;

    @Email(message = "Keine gültige E-Mail-Adresse")
    @Size(max = 100, message = "Maximal 100 Zeichen")
    private String email;

    @NotNull(message = "Datum ist erforderlich")
    @FutureOrPresent(message = "Darf nicht in der Vergangenheit liegen")
    private LocalDate gueltigAb;

    // --- neue Anschrift ---
    @NotNull(message = "Land ist erforderlich")
    private Land land;

    @NotNull(message = "Bundesland/Kanton ist erforderlich")
    private Region region;

    @NotBlank(message = "Straße ist erforderlich")
    @Size(max = 80, message = "Maximal 80 Zeichen")
    private String strasse;

    @NotBlank(message = "Hausnummer ist erforderlich")
    @Size(max = 10, message = "Maximal 10 Zeichen")
    private String hausnummer;

    @NotBlank(message = "PLZ ist erforderlich")
    private String plz;

    @NotBlank(message = "Ort ist erforderlich")
    @Size(max = 80, message = "Maximal 80 Zeichen")
    private String ort;

    // --- optionale abweichende Postanschrift ---
    private boolean abweichendePostanschrift;

    @Size(max = 80, message = "Maximal 80 Zeichen")
    private String postStrasse;

    @Size(max = 10, message = "Maximal 10 Zeichen")
    private String postHausnummer;

    private String postPlz;

    @Size(max = 80, message = "Maximal 80 Zeichen")
    private String postOrt;

    // ---------------------------------------------------------------------
    // Feldübergreifende Regeln (werden von UI und Backend gemeinsam genutzt)
    // ---------------------------------------------------------------------

    @AssertTrue(message = "Bundesland/Kanton passt nicht zum gewählten Land")
    public boolean isRegionPasstZuLand() {
        return land == null || region == null || region.getLand() == land;
    }

    @AssertTrue(message = "PLZ entspricht nicht dem Format des gewählten Landes")
    public boolean isPlzPasstZuLand() {
        return land == null || plz == null || plz.isBlank() || land.istGueltigePlz(plz);
    }

    @AssertTrue(message = "Bei abweichender Postanschrift müssen Straße, Hausnummer, PLZ und Ort angegeben werden")
    public boolean isPostanschriftVollstaendig() {
        if (!abweichendePostanschrift) {
            return true;
        }
        return istGefuellt(postStrasse) && istGefuellt(postHausnummer) && istGefuellt(postPlz) && istGefuellt(postOrt);
    }

    @AssertTrue(message = "PLZ der Postanschrift entspricht nicht dem Format des gewählten Landes")
    public boolean isPostPlzPasstZuLand() {
        return !abweichendePostanschrift || land == null || !istGefuellt(postPlz) || land.istGueltigePlz(postPlz);
    }

    private static boolean istGefuellt(String wert) {
        return wert != null && !wert.isBlank();
    }

    // --- Getter/Setter (Binder benötigt Bean-Properties) ---

    public String getKundennummer() {
        return kundennummer;
    }

    public void setKundennummer(String kundennummer) {
        this.kundennummer = kundennummer;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getGueltigAb() {
        return gueltigAb;
    }

    public void setGueltigAb(LocalDate gueltigAb) {
        this.gueltigAb = gueltigAb;
    }

    public Land getLand() {
        return land;
    }

    public void setLand(Land land) {
        this.land = land;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getHausnummer() {
        return hausnummer;
    }

    public void setHausnummer(String hausnummer) {
        this.hausnummer = hausnummer;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public boolean isAbweichendePostanschrift() {
        return abweichendePostanschrift;
    }

    public void setAbweichendePostanschrift(boolean abweichendePostanschrift) {
        this.abweichendePostanschrift = abweichendePostanschrift;
    }

    public String getPostStrasse() {
        return postStrasse;
    }

    public void setPostStrasse(String postStrasse) {
        this.postStrasse = postStrasse;
    }

    public String getPostHausnummer() {
        return postHausnummer;
    }

    public void setPostHausnummer(String postHausnummer) {
        this.postHausnummer = postHausnummer;
    }

    public String getPostPlz() {
        return postPlz;
    }

    public void setPostPlz(String postPlz) {
        this.postPlz = postPlz;
    }

    public String getPostOrt() {
        return postOrt;
    }

    public void setPostOrt(String postOrt) {
        this.postOrt = postOrt;
    }
}
