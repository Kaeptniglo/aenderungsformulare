package com.demo.formulare.formular;

import com.demo.formulare.dokument.SimulierterDokumentClient;
import com.demo.formulare.model.Adressaenderung;
import com.demo.formulare.model.Bankverbindungsaenderung;
import com.demo.formulare.model.Land;
import com.demo.formulare.model.Region;
import com.demo.formulare.model.Vertragsaenderung;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormularServiceTest {

    private FormularService service;
    private EinreichungRepository repository;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = JsonMapper.builder().build();
        repository = new EinreichungRepository();
        service = new FormularService(
                Validation.buildDefaultValidatorFactory().getValidator(),
                mapper,
                new SimulierterDokumentClient(mapper),
                repository);
    }

    @Test
    void gueltigeAdressaenderungWirdEingereicht() {
        Adressaenderung a = gueltigeAdressaenderung();

        FormularEinreichung einreichung = service.einreichen(FormularTyp.ADRESSAENDERUNG, a);

        assertEquals(FormularTyp.ADRESSAENDERUNG, einreichung.typ());
        assertTrue(einreichung.dokument().dokumentId().startsWith("SIM-"));
        assertEquals("SIMULIERT", einreichung.dokument().status());
        assertEquals("K-123456", einreichung.daten().get("kundennummer"));
        assertEquals("BAYERN", einreichung.daten().get("region"));
        assertEquals(1, repository.alle().size());
        // Regel-Methoden (isXyz) dürfen nicht in den Nutzdaten landen
        assertFalse(einreichung.daten().containsKey("regionPasstZuLand"));
    }

    @Test
    void regionMussZumLandPassen() {
        Adressaenderung a = gueltigeAdressaenderung();
        a.setRegion(Region.WIEN);

        FormularValidierungException ex = assertThrows(FormularValidierungException.class,
                () -> service.einreichen(FormularTyp.ADRESSAENDERUNG, a));

        assertTrue(ex.getFehler().stream().anyMatch(f -> f.contains("passt nicht zum gewählten Land")), ex.getFehler().toString());
        assertTrue(repository.alle().isEmpty());
    }

    @Test
    void plzMussZumLandPassen() {
        Adressaenderung a = gueltigeAdressaenderung();
        a.setLand(Land.OESTERREICH);
        a.setRegion(Region.WIEN);
        a.setPlz("80331"); // 5-stellig, in Österreich ungültig

        FormularValidierungException ex = assertThrows(FormularValidierungException.class,
                () -> service.einreichen(FormularTyp.ADRESSAENDERUNG, a));
        assertTrue(ex.getFehler().stream().anyMatch(f -> f.contains("PLZ")), ex.getFehler().toString());
    }

    @Test
    void bicIstBeiAuslaendischerIbanPflicht() {
        Bankverbindungsaenderung b = new Bankverbindungsaenderung();
        b.setKundennummer("K-123456");
        b.setKontoinhaber("Erika Mustermann");
        b.setIban("AT611904300234573201");
        b.setGueltigAb(LocalDate.now());
        b.setSepaMandatErteilt(true);

        FormularValidierungException ex = assertThrows(FormularValidierungException.class,
                () -> service.einreichen(FormularTyp.BANKVERBINDUNG, b));
        assertTrue(ex.getFehler().stream().anyMatch(f -> f.contains("BIC")), ex.getFehler().toString());

        b.setBic("BKAUATWW");
        assertEquals("SIMULIERT", service.einreichen(FormularTyp.BANKVERBINDUNG, b).dokument().status());
    }

    @Test
    void kuendigungMitSonstigemGrundBrauchtBemerkung() {
        Vertragsaenderung v = new Vertragsaenderung();
        v.setKundennummer("K-123456");
        v.setVertragsnummer("V-12345678");
        v.setSparte(Vertragsaenderung.Sparte.STROM);
        v.setAenderungsart(Vertragsaenderung.Aenderungsart.KUENDIGUNG);
        v.setKuendigungsgrund(Vertragsaenderung.Kuendigungsgrund.SONSTIGES);
        v.setGewuenschtZum(Vertragsaenderung.fruehesterZeitpunkt(LocalDate.now()));

        FormularValidierungException ex = assertThrows(FormularValidierungException.class,
                () -> service.einreichen(FormularTyp.VERTRAGSAENDERUNG, v));
        assertTrue(ex.getFehler().stream().anyMatch(f -> f.contains("Bemerkung")), ex.getFehler().toString());

        v.setBemerkung("Wechsel ins Ausland");
        assertEquals("SIMULIERT", service.einreichen(FormularTyp.VERTRAGSAENDERUNG, v).dokument().status());
    }

    @Test
    void falscherDatentypWirdAbgelehnt() {
        assertThrows(IllegalArgumentException.class,
                () -> service.einreichen(FormularTyp.ADRESSAENDERUNG, new Vertragsaenderung()));
    }

    private static Adressaenderung gueltigeAdressaenderung() {
        Adressaenderung a = new Adressaenderung();
        a.setKundennummer("K-123456");
        a.setVorname("Max");
        a.setNachname("Mustermann");
        a.setGueltigAb(LocalDate.now().plusDays(1));
        a.setLand(Land.DEUTSCHLAND);
        a.setRegion(Region.BAYERN);
        a.setStrasse("Marienplatz");
        a.setHausnummer("1");
        a.setPlz("80331");
        a.setOrt("München");
        return a;
    }
}
