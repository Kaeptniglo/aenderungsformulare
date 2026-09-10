package com.demo.formulare.formular;

import java.util.List;

/**
 * Wird geworfen, wenn die serverseitige Validierung eines Formulars fehlschlägt.
 */
public class FormularValidierungException extends RuntimeException {

    private final List<String> fehler;

    public FormularValidierungException(List<String> fehler) {
        super("Formular ungültig: " + String.join("; ", fehler));
        this.fehler = List.copyOf(fehler);
    }

    public List<String> getFehler() {
        return fehler;
    }
}
