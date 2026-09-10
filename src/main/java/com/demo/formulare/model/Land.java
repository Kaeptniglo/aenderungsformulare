package com.demo.formulare.model;

import java.util.regex.Pattern;

/**
 * Länder, die in den Formularen auswählbar sind. Jedes Land bringt sein eigenes
 * Postleitzahl-Format mit, damit die PLZ-Validierung abhängig vom gewählten Land erfolgen kann.
 */
public enum Land {
    DEUTSCHLAND("Deutschland", "DE", "^\\d{5}$", "5 Ziffern, z. B. 80331"),
    OESTERREICH("Österreich", "AT", "^\\d{4}$", "4 Ziffern, z. B. 1010"),
    SCHWEIZ("Schweiz", "CH", "^\\d{4}$", "4 Ziffern, z. B. 8001");

    private final String bezeichnung;
    private final String isoCode;
    private final Pattern plzMuster;
    private final String plzHinweis;

    Land(String bezeichnung, String isoCode, String plzRegex, String plzHinweis) {
        this.bezeichnung = bezeichnung;
        this.isoCode = isoCode;
        this.plzMuster = Pattern.compile(plzRegex);
        this.plzHinweis = plzHinweis;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getPlzHinweis() {
        return plzHinweis;
    }

    public boolean istGueltigePlz(String plz) {
        return plz != null && plzMuster.matcher(plz.trim()).matches();
    }
}
