package com.demo.formulare.dokument;

/**
 * Fehler bei der Kommunikation mit dem Dokumenterstellungs-System.
 */
public class DokumentErstellungException extends RuntimeException {

    public DokumentErstellungException(String message, Throwable cause) {
        super(message, cause);
    }
}
