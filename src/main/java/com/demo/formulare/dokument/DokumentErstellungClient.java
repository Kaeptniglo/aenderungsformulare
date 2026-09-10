package com.demo.formulare.dokument;

/**
 * Schnittstelle zum Dokumenterstellungs-System. Implementierungen:
 * {@link SimulierterDokumentClient} (Standard im Prototyp) und {@link RestDokumentClient}.
 */
public interface DokumentErstellungClient {

    /**
     * Erstellt aus den übergebenen Formulardaten ein Dokument (PDF).
     *
     * @throws DokumentErstellungException wenn das Zielsystem nicht erreichbar ist oder einen Fehler meldet
     */
    DokumentAntwort erstelleDokument(DokumentAnfrage anfrage);
}
