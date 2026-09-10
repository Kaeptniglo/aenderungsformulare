package com.demo.formulare.dokument;

import java.util.Map;

/**
 * Anfrage an das Dokumenterstellungs-System.
 *
 * @param vorlagenId Kennung der PDF-Vorlage
 * @param titel      Anzeigename des Formulars
 * @param daten      Feldwerte (Feldname -> Wert), die in die Vorlage übernommen werden
 */
public record DokumentAnfrage(String vorlagenId, String titel, Map<String, Object> daten) {
}
