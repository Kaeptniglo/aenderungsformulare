package com.demo.formulare.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.demo.formulare.formular.FormularService;
import com.demo.formulare.formular.FormularTyp;
import com.demo.formulare.model.Vertragsaenderung;
import com.demo.formulare.model.Vertragsaenderung.Aenderungsart;
import com.demo.formulare.model.Vertragsaenderung.Kuendigungsgrund;
import com.demo.formulare.model.Vertragsaenderung.Sparte;
import com.demo.formulare.model.Vertragsaenderung.Tarif;

import java.time.LocalDate;
import java.util.List;

/**
 * Formular "Vertragsänderung".
 * <p>
 * Abhängigkeiten zwischen Feldern:
 * <ul>
 *     <li>Änderungsart → blendet die Abschnitte Tarifwechsel / Vertragsübernahme / Kündigung ein und macht deren Felder zur Pflicht</li>
 *     <li>Sparte → schränkt die wählbaren Tarife ein</li>
 *     <li>Tarif → schränkt die wählbaren Laufzeiten ein</li>
 *     <li>Kündigungsgrund "Sonstiges" → Bemerkung wird Pflicht</li>
 * </ul>
 */
@Route(value = FormularTyp.ROUTE_PRAEFIX + "vertragsaenderung", layout = MainLayout.class)
public class VertragsaenderungView extends AbstractFormularView<Vertragsaenderung> {

    private static final int BEMERKUNG_MAX = 500;

    private final TextField kundennummer = new TextField("Kundennummer");
    private final TextField vertragsnummer = new TextField("Vertragsnummer");
    private final RadioButtonGroup<Aenderungsart> aenderungsart = new RadioButtonGroup<>("Art der Änderung");
    private final ComboBox<Sparte> sparte = new ComboBox<>("Sparte");
    private final DatePicker gewuenschtZum = UiHilfen.datum("Gewünscht zum");

    private H4 tarifAbschnitt;
    private final ComboBox<Tarif> tarif = new ComboBox<>("Neuer Tarif");
    private final ComboBox<Integer> laufzeitMonate = new ComboBox<>("Laufzeit");

    private H4 uebernahmeAbschnitt;
    private final TextField neuerVertragspartnerName = new TextField("Name des neuen Vertragspartners");
    private final DatePicker neuerVertragspartnerGeburtsdatum = UiHilfen.datum("Geburtsdatum des neuen Vertragspartners");

    private H4 kuendigungAbschnitt;
    private final ComboBox<Kuendigungsgrund> kuendigungsgrund = new ComboBox<>("Kündigungsgrund");

    private final TextArea bemerkung = new TextArea("Bemerkung");

    private Binder.Binding<Vertragsaenderung, Tarif> tarifBindung;
    private Binder.Binding<Vertragsaenderung, Integer> laufzeitBindung;
    private Binder.Binding<Vertragsaenderung, String> nameBindung;
    private Binder.Binding<Vertragsaenderung, LocalDate> geburtsdatumBindung;
    private Binder.Binding<Vertragsaenderung, Kuendigungsgrund> kuendigungsgrundBindung;
    private Binder.Binding<Vertragsaenderung, String> bemerkungBindung;

    public VertragsaenderungView(FormularService service) {
        super(FormularTyp.VERTRAGSAENDERUNG, Vertragsaenderung.class, service);
        initialisieren();
    }

    @Override
    protected Vertragsaenderung neuesFormular() {
        return new Vertragsaenderung();
    }

    @Override
    protected void aufbauen(FormLayout form) {
        abschnitt(form, "Vertrag");
        kundennummer.setPlaceholder("K-123456");
        kundennummer.setHelperText("Format: K- gefolgt von 6 Ziffern");
        vertragsnummer.setPlaceholder("V-12345678");
        vertragsnummer.setHelperText("Format: V- gefolgt von 8 Ziffern");
        sparte.setItems(Sparte.values());
        sparte.setItemLabelGenerator(Sparte::getBezeichnung);
        aenderungsart.setItems(Aenderungsart.values());
        aenderungsart.setItemLabelGenerator(Aenderungsart::getBezeichnung);

        LocalDate fruehestens = Vertragsaenderung.fruehesterZeitpunkt(LocalDate.now());
        gewuenschtZum.setMin(fruehestens);
        gewuenschtZum.setHelperText("Monatserster, mindestens " + Vertragsaenderung.MINDESTVORLAUF_TAGE
                + " Tage im Voraus (frühestens " + fruehestens.getDayOfMonth() + "." + fruehestens.getMonthValue()
                + "." + fruehestens.getYear() + ")");

        form.add(kundennummer, vertragsnummer, sparte, gewuenschtZum, aenderungsart);
        volleBreite(form, aenderungsart);

        bindeText(kundennummer, "kundennummer");
        bindeText(vertragsnummer, "vertragsnummer");
        binder.forField(sparte).bind("sparte");
        binder.forField(aenderungsart).bind("aenderungsart");
        binder.forField(gewuenschtZum)
                .withValidator(d -> d == null || d.getDayOfMonth() == 1, "Muss ein Monatserster sein")
                .withValidator(d -> d == null || !d.isBefore(fruehestens),
                        "Mindestens " + Vertragsaenderung.MINDESTVORLAUF_TAGE + " Tage Vorlauf erforderlich")
                .bind("gewuenschtZum");

        // --- Tarifwechsel ---
        tarifAbschnitt = abschnitt(form, "Tarifwechsel");
        tarif.setItemLabelGenerator(Tarif::getBezeichnung);
        tarif.setPlaceholder("Zuerst Sparte wählen");
        laufzeitMonate.setItemLabelGenerator(m -> m + (m == 1 ? " Monat" : " Monate"));
        laufzeitMonate.setPlaceholder("Zuerst Tarif wählen");
        form.add(tarif, laufzeitMonate);

        tarifBindung = binder.forField(tarif)
                .withValidator(t -> !istArt(Aenderungsart.TARIFWECHSEL) || t != null, "Tarif ist erforderlich")
                .withValidator(t -> t == null || sparte.getValue() == null || t.getSparte() == sparte.getValue(),
                        "Tarif passt nicht zur Sparte")
                .bind("tarif");
        laufzeitBindung = binder.forField(laufzeitMonate)
                .withValidator(m -> !istArt(Aenderungsart.TARIFWECHSEL) || m != null, "Laufzeit ist erforderlich")
                .withValidator(m -> m == null || tarif.getValue() == null || tarif.getValue().getLaufzeiten().contains(m),
                        "Laufzeit für diesen Tarif nicht verfügbar")
                .bind("laufzeitMonate");

        sparte.addValueChangeListener(e -> sparteGeaendert(e.getValue()));
        tarif.addValueChangeListener(e -> tarifGeaendert(e.getValue()));

        // --- Vertragsübernahme ---
        uebernahmeAbschnitt = abschnitt(form, "Vertragsübernahme");
        LocalDate spaetestesGeburtsdatum = LocalDate.now().minusYears(Vertragsaenderung.MINDESTALTER_JAHRE);
        neuerVertragspartnerGeburtsdatum.setMax(spaetestesGeburtsdatum);
        neuerVertragspartnerGeburtsdatum.setHelperText("Der neue Vertragspartner muss volljährig sein");
        form.add(neuerVertragspartnerName, neuerVertragspartnerGeburtsdatum);

        nameBindung = binder.forField(neuerVertragspartnerName)
                .withNullRepresentation("")
                .withValidator(v -> !istArt(Aenderungsart.VERTRAGSUEBERNAHME) || !UiHilfen.leer(v),
                        "Name ist bei Vertragsübernahme erforderlich")
                .bind("neuerVertragspartnerName");
        geburtsdatumBindung = binder.forField(neuerVertragspartnerGeburtsdatum)
                .withValidator(d -> !istArt(Aenderungsart.VERTRAGSUEBERNAHME) || d != null,
                        "Geburtsdatum ist bei Vertragsübernahme erforderlich")
                .withValidator(d -> d == null || !d.isAfter(spaetestesGeburtsdatum),
                        "Der neue Vertragspartner muss mindestens " + Vertragsaenderung.MINDESTALTER_JAHRE + " Jahre alt sein")
                .bind("neuerVertragspartnerGeburtsdatum");

        // --- Kündigung ---
        kuendigungAbschnitt = abschnitt(form, "Kündigung");
        kuendigungsgrund.setItems(Kuendigungsgrund.values());
        kuendigungsgrund.setItemLabelGenerator(Kuendigungsgrund::getBezeichnung);
        form.add(kuendigungsgrund);

        kuendigungsgrundBindung = binder.forField(kuendigungsgrund)
                .withValidator(g -> !istArt(Aenderungsart.KUENDIGUNG) || g != null, "Kündigungsgrund ist erforderlich")
                .bind("kuendigungsgrund");
        kuendigungsgrund.addValueChangeListener(e -> bemerkungAktualisieren());

        // --- Bemerkung ---
        abschnitt(form, "Sonstiges");
        bemerkung.setMaxLength(BEMERKUNG_MAX);
        bemerkung.setValueChangeMode(ValueChangeMode.EAGER);
        bemerkung.addValueChangeListener(e -> bemerkung.setHelperText(e.getValue().length() + " / " + BEMERKUNG_MAX));
        form.add(bemerkung);
        volleBreite(form, bemerkung);

        bemerkungBindung = binder.forField(bemerkung)
                .withNullRepresentation("")
                .withValidator(v -> !bemerkungErforderlich() || !UiHilfen.leer(v),
                        "Bei Kündigungsgrund \"Sonstiges\" ist eine Bemerkung erforderlich")
                .bind("bemerkung");

        aenderungsart.addValueChangeListener(e -> artGeaendert(e.getValue()));
    }

    @Override
    protected void nachNeubeginn() {
        sparteGeaendert(sparte.getValue());
        tarifGeaendert(tarif.getValue());
        artGeaendert(aenderungsart.getValue());
        bemerkung.setHelperText("0 / " + BEMERKUNG_MAX);
    }

    private boolean istArt(Aenderungsart art) {
        return aenderungsart.getValue() == art;
    }

    private boolean bemerkungErforderlich() {
        return istArt(Aenderungsart.KUENDIGUNG) && kuendigungsgrund.getValue() == Kuendigungsgrund.SONSTIGES;
    }

    /** Änderungsart blendet Abschnitte ein/aus; ausgeblendete Felder werden geleert. */
    private void artGeaendert(Aenderungsart art) {
        boolean tarifwechsel = art == Aenderungsart.TARIFWECHSEL;
        boolean uebernahme = art == Aenderungsart.VERTRAGSUEBERNAHME;
        boolean kuendigung = art == Aenderungsart.KUENDIGUNG;

        sichtbarkeit(tarifwechsel, tarifAbschnitt, tarif, laufzeitMonate);
        sichtbarkeit(uebernahme, uebernahmeAbschnitt, neuerVertragspartnerName, neuerVertragspartnerGeburtsdatum);
        sichtbarkeit(kuendigung, kuendigungAbschnitt, kuendigungsgrund);

        tarif.setRequiredIndicatorVisible(tarifwechsel);
        laufzeitMonate.setRequiredIndicatorVisible(tarifwechsel);
        neuerVertragspartnerName.setRequiredIndicatorVisible(uebernahme);
        neuerVertragspartnerGeburtsdatum.setRequiredIndicatorVisible(uebernahme);
        kuendigungsgrund.setRequiredIndicatorVisible(kuendigung);

        // Ausgeblendete Felder leeren und neu validieren (entfernt alte Fehlermeldungen).
        // Neu eingeblendete Felder werden bewusst nicht validiert, damit keine Fehler vor der ersten Eingabe erscheinen.
        if (!tarifwechsel) {
            tarif.clear();
            laufzeitMonate.clear();
            tarifBindung.validate();
            laufzeitBindung.validate();
        }
        if (!uebernahme) {
            neuerVertragspartnerName.clear();
            neuerVertragspartnerGeburtsdatum.clear();
            nameBindung.validate();
            geburtsdatumBindung.validate();
        }
        if (!kuendigung) {
            kuendigungsgrund.clear();
            kuendigungsgrundBindung.validate();
        }
        bemerkungAktualisieren();
    }

    private void sichtbarkeit(boolean sichtbar, Component... komponenten) {
        for (Component k : komponenten) {
            k.setVisible(sichtbar);
        }
    }

    /** Sparte schränkt die Tarife ein. */
    private void sparteGeaendert(Sparte neueSparte) {
        tarif.setItems(Tarif.fuer(neueSparte));
        tarif.setEnabled(neueSparte != null);
        tarif.setPlaceholder(neueSparte == null ? "Zuerst Sparte wählen" : "Tarif wählen");
        if (tarif.getValue() != null && tarif.getValue().getSparte() != neueSparte) {
            tarif.clear();
        }
    }

    /** Tarif schränkt die Laufzeiten ein. */
    private void tarifGeaendert(Tarif neuerTarif) {
        List<Integer> laufzeiten = neuerTarif == null ? List.of() : neuerTarif.getLaufzeiten();
        laufzeitMonate.setItems(laufzeiten);
        laufzeitMonate.setEnabled(neuerTarif != null);
        laufzeitMonate.setPlaceholder(neuerTarif == null ? "Zuerst Tarif wählen" : "Laufzeit wählen");
        if (laufzeitMonate.getValue() != null && !laufzeiten.contains(laufzeitMonate.getValue())) {
            laufzeitMonate.clear();
        }
        if (laufzeiten.size() == 1) {
            laufzeitMonate.setValue(laufzeiten.get(0));
        }
    }

    private void bemerkungAktualisieren() {
        boolean erforderlich = bemerkungErforderlich();
        bemerkung.setRequiredIndicatorVisible(erforderlich);
        bemerkung.setLabel(erforderlich ? "Bemerkung (Begründung der Kündigung)" : "Bemerkung");
        if (!erforderlich || !bemerkung.isEmpty()) {
            bemerkungBindung.validate();
        }
    }
}
