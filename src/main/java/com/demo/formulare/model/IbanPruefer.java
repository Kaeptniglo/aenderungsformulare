package com.demo.formulare.model;

import java.math.BigInteger;
import java.util.Locale;

/**
 * Prüft IBANs nach ISO 13616 (Länge, Zeichen, Mod-97-Prüfsumme).
 */
public final class IbanPruefer {

    private IbanPruefer() {
    }

    /** Entfernt Leerzeichen und wandelt in Großbuchstaben um. */
    public static String normalisieren(String iban) {
        if (iban == null) {
            return null;
        }
        return iban.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    /** Ländercode (die ersten zwei Buchstaben) oder {@code null}, falls nicht ermittelbar. */
    public static String laenderCode(String iban) {
        String n = normalisieren(iban);
        if (n == null || n.length() < 2) {
            return null;
        }
        return n.substring(0, 2);
    }

    public static boolean istGueltig(String iban) {
        String n = normalisieren(iban);
        if (n == null || n.length() < 15 || n.length() > 34) {
            return false;
        }
        if (!n.matches("^[A-Z]{2}\\d{2}[A-Z0-9]+$")) {
            return false;
        }
        // Ländercode + Prüfziffern ans Ende verschieben, Buchstaben in Zahlen (A=10 ... Z=35) wandeln
        String umgestellt = n.substring(4) + n.substring(0, 4);
        StringBuilder ziffern = new StringBuilder();
        for (char c : umgestellt.toCharArray()) {
            if (Character.isDigit(c)) {
                ziffern.append(c);
            } else {
                ziffern.append(c - 'A' + 10);
            }
        }
        return new BigInteger(ziffern.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }
}
