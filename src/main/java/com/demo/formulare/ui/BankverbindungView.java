package com.demo.formulare.ui;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.demo.formulare.formular.FormularService;
import com.demo.formulare.formular.FormularTyp;
import com.demo.formulare.model.Bankverbindungsaenderung;
import com.demo.formulare.model.Bankverbindungsaenderung.Beziehung;
import com.demo.formulare.model.IbanPruefer;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Formular "Änderung der Bankverbindung".
 * <p>
 * Abhängigkeiten zwischen Feldern:
 * <ul>
 *     <li>IBAN → BIC ist nur bei nicht-deutscher IBAN Pflicht</li>
 *     <li>Checkbox "abweichender Kontoinhaber" → Beziehung zum Kunden wird Pflicht</li>
 * </ul>
 */
@Route(value = FormularTyp.ROUTE_PRAEFIX + "bankverbindung", layout = MainLayout.class)
public class BankverbindungView extends AbstractFormularView<Bankverbindungsaenderung> {

    private final TextField kundennummer = new TextField("Kundennummer");
    private final DatePicker gueltigAb = UiHilfen.datum("Gültig ab");
    private final TextField kontoinhaber = new TextField("Kontoinhaber");
    private final TextField iban = new TextField("IBAN");
    private final TextField bic = new TextField("BIC");
    private final TextField bankname = new TextField("Name der Bank");
    private final Checkbox abweichenderKontoinhaber = new Checkbox("Kontoinhaber ist nicht der Kunde");
    private final ComboBox<Beziehung> beziehungZumKunden = new ComboBox<>("Beziehung zum Kunden");
    private final Checkbox sepaMandat = new Checkbox(
            "Ich ermächtige den Zahlungsempfänger, Zahlungen per SEPA-Lastschrift einzuziehen.");

    private Binder.Binding<Bankverbindungsaenderung, String> bicBindung;
    private Binder.Binding<Bankverbindungsaenderung, Beziehung> beziehungBindung;

    public BankverbindungView(FormularService service) {
        super(FormularTyp.BANKVERBINDUNG, Bankverbindungsaenderung.class, service);
        initialisieren();
    }

    @Override
    protected Bankverbindungsaenderung neuesFormular() {
        Bankverbindungsaenderung b = new Bankverbindungsaenderung();
        b.setGueltigAb(LocalDate.now());
        return b;
    }

    @Override
    protected void aufbauen(FormLayout form) {
        abschnitt(form, "Kunde");
        kundennummer.setPlaceholder("K-123456");
        kundennummer.setHelperText("Format: K- gefolgt von 6 Ziffern");
        gueltigAb.setMin(LocalDate.now());
        form.add(kundennummer, gueltigAb);
        bindeText(kundennummer, "kundennummer");
        binder.forField(gueltigAb).bind("gueltigAb");

        abschnitt(form, "Neue Bankverbindung");
        iban.setPlaceholder("DE89 3704 0044 0532 0130 00");
        iban.setValueChangeMode(ValueChangeMode.LAZY);
        bic.setPlaceholder("COBADEFFXXX");
        form.add(kontoinhaber, bankname, iban, bic);

        bindeText(kontoinhaber, "kontoinhaber");
        bindeText(bankname, "bankname");
        binder.forField(iban)
                .withConverter(UiHilfen.normalisiert(IbanPruefer::normalisieren))
                .withValidator(v -> v == null || IbanPruefer.istGueltig(v), "IBAN ist ungültig (Prüfsumme/Format)")
                .bind("iban");
        bicBindung = binder.forField(bic)
                .withConverter(UiHilfen.normalisiert(v -> v.replaceAll("\\s+", "").toUpperCase(Locale.ROOT)))
                .withValidator(v -> !bicErforderlich() || v != null, "BIC ist bei ausländischer IBAN erforderlich")
                .withValidator(v -> v == null || v.matches(Bankverbindungsaenderung.BIC_REGEX),
                        "BIC muss 8 oder 11 Zeichen haben, z. B. COBADEFFXXX")
                .bind("bic");

        iban.addValueChangeListener(e -> bicAktualisieren());

        abschnitt(form, "Kontoinhaber");
        form.add(abweichenderKontoinhaber);
        volleBreite(form, abweichenderKontoinhaber);
        beziehungZumKunden.setItems(Beziehung.values());
        beziehungZumKunden.setItemLabelGenerator(Beziehung::getBezeichnung);
        form.add(beziehungZumKunden);

        binder.forField(abweichenderKontoinhaber).bind("abweichenderKontoinhaber");
        beziehungBindung = binder.forField(beziehungZumKunden)
                .withValidator(v -> !abweichenderKontoinhaber.getValue() || v != null,
                        "Bei abweichendem Kontoinhaber erforderlich")
                .bind("beziehungZumKunden");
        abweichenderKontoinhaber.addValueChangeListener(e -> kontoinhaberUmschalten(e.getValue()));

        abschnitt(form, "SEPA-Lastschriftmandat");
        form.add(sepaMandat);
        volleBreite(form, sepaMandat);
        binder.forField(sepaMandat)
                .withValidator(Boolean::booleanValue, "Das SEPA-Lastschriftmandat muss erteilt werden")
                .bind("sepaMandatErteilt");
    }

    @Override
    protected void nachNeubeginn() {
        bicAktualisieren();
        kontoinhaberUmschalten(abweichenderKontoinhaber.getValue());
    }

    private boolean bicErforderlich() {
        String code = IbanPruefer.laenderCode(iban.getValue());
        return code != null && !"DE".equals(code);
    }

    /** IBAN-Ländercode bestimmt, ob die BIC Pflicht ist. */
    private void bicAktualisieren() {
        boolean erforderlich = bicErforderlich();
        bic.setRequiredIndicatorVisible(erforderlich);
        bic.setHelperText(erforderlich
                ? "Pflicht, da die IBAN nicht aus Deutschland stammt"
                : "Optional bei deutscher IBAN");
        // Nicht validieren, solange die BIC leer ist und gerade erst Pflicht wurde (kein Fehler vor der Eingabe)
        if (!erforderlich || !bic.isEmpty()) {
            bicBindung.validate();
        }
    }

    private void kontoinhaberUmschalten(boolean abweichend) {
        beziehungZumKunden.setEnabled(abweichend);
        beziehungZumKunden.setRequiredIndicatorVisible(abweichend);
        if (!abweichend) {
            beziehungZumKunden.clear();
            beziehungBindung.validate();
        }
    }
}
