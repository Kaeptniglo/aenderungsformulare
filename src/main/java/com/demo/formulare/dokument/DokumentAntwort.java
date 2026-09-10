package com.demo.formulare.dokument;

/**
 * Antwort des Dokumenterstellungs-Systems.
 *
 * @param dokumentId Kennung des erzeugten Dokuments
 * @param dateiname  Dateiname des PDFs
 * @param status     Status, z. B. {@code ERSTELLT} oder {@code SIMULIERT}
 */
public record DokumentAntwort(String dokumentId, String dateiname, String status) {
}
