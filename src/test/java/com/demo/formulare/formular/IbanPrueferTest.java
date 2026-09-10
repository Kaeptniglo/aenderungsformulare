package com.demo.formulare.formular;

import com.demo.formulare.model.IbanPruefer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IbanPrueferTest {

    @Test
    void gueltigeIbansWerdenErkannt() {
        assertTrue(IbanPruefer.istGueltig("DE89370400440532013000"));
        assertTrue(IbanPruefer.istGueltig("de89 3704 0044 0532 0130 00"));
        assertTrue(IbanPruefer.istGueltig("AT611904300234573201"));
        assertTrue(IbanPruefer.istGueltig("CH9300762011623852957"));
    }

    @Test
    void ungueltigeIbansWerdenAbgelehnt() {
        assertFalse(IbanPruefer.istGueltig(null));
        assertFalse(IbanPruefer.istGueltig(""));
        assertFalse(IbanPruefer.istGueltig("DE89370400440532013001")); // Prüfsumme falsch
        assertFalse(IbanPruefer.istGueltig("DE8937040044"));           // zu kurz
        assertFalse(IbanPruefer.istGueltig("1234567890123456"));       // kein Ländercode
    }

    @Test
    void normalisierungUndLaendercode() {
        assertEquals("DE89370400440532013000", IbanPruefer.normalisieren(" de89 3704 0044 0532 0130 00 "));
        assertEquals("AT", IbanPruefer.laenderCode("at61 1904 3002 3457 3201"));
        assertEquals(null, IbanPruefer.laenderCode("D"));
    }
}
