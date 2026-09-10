package com.demo.formulare.ui;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.demo.formulare.formular.FormularService;
import com.demo.formulare.formular.FormularTyp;
import com.demo.formulare.model.Adressaenderung;
import com.demo.formulare.model.Land;
import com.demo.formulare.model.Region;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Formular "Adressänderung".
 * <p>
 * Abhängigkeiten zwischen Feldern:
 * <ul>
 *     <li>Land → schränkt die Auswahl Bundesland/Kanton ein und bestimmt das PLZ-Format</li>
 *     <li>Checkbox "abweichende Postanschrift" → schaltet die Postanschrift-Felder frei und macht sie zur Pflicht</li>
 * </ul>
 */
@Route(value = FormularTyp.ROUTE_PRAEFIX + "adressaenderung", layout = MainLayout.class)
public class AdressaenderungView extends AbstractFormularView<Adressaenderung> {

    private final TextField kundennummer = new TextField("Kundennummer");
    private final TextField vorname = new TextField("Vorname");
    private final TextField nachname = new TextField("Nachname");
    private final EmailField email = new EmailField("E-Mail (für Bestätigung)");
    private final DatePicker gueltigAb = UiHilfen.datum("Gültig ab");

    private final ComboBox<Land> land = new ComboBox<>("Land");
    private final ComboBox<Region> region = new ComboBox<>("Bundesland / Kanton");
    private final TextField strasse = new TextField("Straße");
    private final TextField hausnummer = new TextField("Hausnummer");
    private final TextField plz = new TextField("PLZ");
    private final TextField ort = new TextField("Ort");

    private final Checkbox abweichendePostanschrift = new Checkbox("Abweichende Postanschrift");
    private final TextField postStrasse = new TextField("Straße (Post)");
    private final TextField postHausnummer = new TextField("Hausnummer (Post)");
    private final TextField postPlz = new TextField("PLZ (Post)");
    private final TextField postOrt = new TextField("Ort (Post)");

    private Binder.Binding<Adressaenderung, String> plzBindung;
    private Binder.Binding<Adressaenderung, Region> regionBindung;
    private final List<Binder.Binding<Adressaenderung, String>> postBindungen = new ArrayList<>();

    public AdressaenderungView(FormularService service) {
        super(FormularTyp.ADRESSAENDERUNG, Adressaenderung.class, service);
        initialisieren();
    }

    @Override
    protected Adressaenderung neuesFormular() {
        Adressaenderung a = new Adressaenderung();
        a.setGueltigAb(LocalDate.now());
        return a;
    }

    @Override
    protected void aufbauen(FormLayout form) {
        // --- Abschnitt Kunde ---
        abschnitt(form, "Kunde");
        kundennummer.setPlaceholder("K-123456");
        kundennummer.setHelperText("Format: K- gefolgt von 6 Ziffern");
        gueltigAb.setMin(LocalDate.now());
        gueltigAb.setHelperText("Frühestens heute");
        form.add(kundennummer, gueltigAb, vorname, nachname, email);

        bindeText(kundennummer, "kundennummer");
        bindeText(vorname, "vorname");
        bindeText(nachname, "nachname");
        binder.forField(email).withNullRepresentation("").bind("email");
        binder.forField(gueltigAb).bind("gueltigAb");

        // --- Abschnitt neue Anschrift ---
        abschnitt(form, "Neue Anschrift");
        land.setItems(Land.values());
        land.setItemLabelGenerator(Land::getBezeichnung);
        region.setItemLabelGenerator(Region::getBezeichnung);
        region.setPlaceholder("Zuerst Land wählen");
        form.add(land, region, strasse, hausnummer, plz, ort);

        binder.forField(land).bind("land");
        regionBindung = binder.forField(region)
                .withValidator(r -> r == null || land.getValue() == null || r.getLand() == land.getValue(),
                        "Bundesland/Kanton passt nicht zum gewählten Land")
                .bind("region");
        bindeText(strasse, "strasse");
        bindeText(hausnummer, "hausnummer");
        plzBindung = binder.forField(plz)
                .withNullRepresentation("")
                .withValidator(this::plzPasstZumLand, "PLZ entspricht nicht dem Format des gewählten Landes")
                .bind("plz");
        bindeText(ort, "ort");

        land.addValueChangeListener(e -> landGeaendert(e.getValue()));

        // --- Abschnitt Postanschrift ---
        abschnitt(form, "Postanschrift");
        form.add(abweichendePostanschrift);
        volleBreite(form, abweichendePostanschrift);
        form.add(postStrasse, postHausnummer, postPlz, postOrt);

        binder.forField(abweichendePostanschrift).bind("abweichendePostanschrift");
        postBindungen.add(bindePostFeld(postStrasse, "postStrasse"));
        postBindungen.add(bindePostFeld(postHausnummer, "postHausnummer"));
        postBindungen.add(binder.forField(postPlz)
                .withNullRepresentation("")
                .withValidator(v -> !abweichendePostanschrift.getValue() || !UiHilfen.leer(v),
                        "Erforderlich bei abweichender Postanschrift")
                .withValidator(this::plzPasstZumLand, "PLZ entspricht nicht dem Format des gewählten Landes")
                .bind("postPlz"));
        postBindungen.add(bindePostFeld(postOrt, "postOrt"));

        abweichendePostanschrift.addValueChangeListener(e -> postanschriftUmschalten(e.getValue()));

        // Sicherheitsnetz auf Modellebene (gleiche Regeln wie im Backend); Meldung erscheint in der Statuszeile
        binder.withValidator(Adressaenderung::isRegionPasstZuLand, "Bundesland/Kanton passt nicht zum Land");
        binder.withValidator(Adressaenderung::isPostanschriftVollstaendig, "Postanschrift ist unvollständig");
    }

    @Override
    protected void nachNeubeginn() {
        landGeaendert(land.getValue());
        postanschriftUmschalten(abweichendePostanschrift.getValue());
    }

    private Binder.Binding<Adressaenderung, String> bindePostFeld(TextField feld, String property) {
        return binder.forField(feld)
                .withNullRepresentation("")
                .withValidator(v -> !abweichendePostanschrift.getValue() || !UiHilfen.leer(v),
                        "Erforderlich bei abweichender Postanschrift")
                .bind(property);
    }

    private boolean plzPasstZumLand(String wert) {
        return UiHilfen.leer(wert) || land.getValue() == null || land.getValue().istGueltigePlz(wert);
    }

    /** Land beeinflusst Regionen-Auswahl und PLZ-Format. */
    private void landGeaendert(Land neuesLand) {
        region.setItems(Region.fuer(neuesLand));
        region.setEnabled(neuesLand != null);
        region.setPlaceholder(neuesLand == null ? "Zuerst Land wählen" : "Bundesland/Kanton wählen");
        if (region.getValue() != null && region.getValue().getLand() != neuesLand) {
            region.clear();
        }
        String hinweis = neuesLand == null ? "" : neuesLand.getPlzHinweis();
        plz.setHelperText(hinweis);
        postPlz.setHelperText(abweichendePostanschrift.getValue() ? hinweis : "");

        // bereits eingegebene Werte gegen das neue Land prüfen
        if (!plz.isEmpty()) {
            plzBindung.validate();
        }
        if (!postPlz.isEmpty()) {
            postBindungen.forEach(Binder.Binding::validate);
        }
        if (region.getValue() != null) {
            regionBindung.validate();
        }
    }

    /** Checkbox schaltet die Postanschrift-Felder frei bzw. leert und sperrt sie. */
    private void postanschriftUmschalten(boolean aktiv) {
        for (TextField feld : List.of(postStrasse, postHausnummer, postPlz, postOrt)) {
            feld.setEnabled(aktiv);
            feld.setRequiredIndicatorVisible(aktiv);
            if (!aktiv) {
                feld.clear();
            }
        }
        postPlz.setHelperText(aktiv && land.getValue() != null ? land.getValue().getPlzHinweis() : "");
        if (!aktiv) {
            postBindungen.forEach(Binder.Binding::validate);
        }
    }
}
