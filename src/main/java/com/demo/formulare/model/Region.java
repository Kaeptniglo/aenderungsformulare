package com.demo.formulare.model;

import java.util.Arrays;
import java.util.List;

/**
 * Bundesland bzw. Kanton. Die Auswahl im Formular wird auf die Regionen des gewählten Landes eingeschränkt.
 */
public enum Region {
    // Deutschland
    BADEN_WUERTTEMBERG("Baden-Württemberg", Land.DEUTSCHLAND),
    BAYERN("Bayern", Land.DEUTSCHLAND),
    BERLIN("Berlin", Land.DEUTSCHLAND),
    BRANDENBURG("Brandenburg", Land.DEUTSCHLAND),
    BREMEN("Bremen", Land.DEUTSCHLAND),
    HAMBURG("Hamburg", Land.DEUTSCHLAND),
    HESSEN("Hessen", Land.DEUTSCHLAND),
    MECKLENBURG_VORPOMMERN("Mecklenburg-Vorpommern", Land.DEUTSCHLAND),
    NIEDERSACHSEN("Niedersachsen", Land.DEUTSCHLAND),
    NORDRHEIN_WESTFALEN("Nordrhein-Westfalen", Land.DEUTSCHLAND),
    RHEINLAND_PFALZ("Rheinland-Pfalz", Land.DEUTSCHLAND),
    SAARLAND("Saarland", Land.DEUTSCHLAND),
    SACHSEN("Sachsen", Land.DEUTSCHLAND),
    SACHSEN_ANHALT("Sachsen-Anhalt", Land.DEUTSCHLAND),
    SCHLESWIG_HOLSTEIN("Schleswig-Holstein", Land.DEUTSCHLAND),
    THUERINGEN("Thüringen", Land.DEUTSCHLAND),
    // Österreich
    BURGENLAND("Burgenland", Land.OESTERREICH),
    KAERNTEN("Kärnten", Land.OESTERREICH),
    NIEDEROESTERREICH("Niederösterreich", Land.OESTERREICH),
    OBEROESTERREICH("Oberösterreich", Land.OESTERREICH),
    SALZBURG("Salzburg", Land.OESTERREICH),
    STEIERMARK("Steiermark", Land.OESTERREICH),
    TIROL("Tirol", Land.OESTERREICH),
    VORARLBERG("Vorarlberg", Land.OESTERREICH),
    WIEN("Wien", Land.OESTERREICH),
    // Schweiz (Auszug)
    BASEL_STADT("Basel-Stadt", Land.SCHWEIZ),
    BERN("Bern", Land.SCHWEIZ),
    GENF("Genf", Land.SCHWEIZ),
    LUZERN("Luzern", Land.SCHWEIZ),
    ST_GALLEN("St. Gallen", Land.SCHWEIZ),
    ZUERICH("Zürich", Land.SCHWEIZ);

    private final String bezeichnung;
    private final Land land;

    Region(String bezeichnung, Land land) {
        this.bezeichnung = bezeichnung;
        this.land = land;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public Land getLand() {
        return land;
    }

    /** Alle Regionen eines Landes, alphabetisch sortiert. */
    public static List<Region> fuer(Land land) {
        if (land == null) {
            return List.of();
        }
        return Arrays.stream(values())
                .filter(r -> r.land == land)
                .sorted((a, b) -> a.bezeichnung.compareToIgnoreCase(b.bezeichnung))
                .toList();
    }
}
