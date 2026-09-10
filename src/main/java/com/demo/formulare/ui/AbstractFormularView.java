package com.demo.formulare.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.demo.formulare.dokument.DokumentErstellungException;
import com.demo.formulare.formular.FormularEinreichung;
import com.demo.formulare.formular.FormularService;
import com.demo.formulare.formular.FormularTyp;
import com.demo.formulare.formular.FormularValidierungException;

/**
 * Gemeinsame Basis aller Formular-Views: Kopf, Formularlayout, Statuszeile, Absenden/Zurücksetzen,
 * Anbindung an den {@link FormularService}.
 * <p>
 * Ablauf für eine konkrete View:
 * <ol>
 *     <li>Felder als Instanzvariablen anlegen</li>
 *     <li>im Konstruktor {@link #initialisieren()} aufrufen</li>
 *     <li>in {@link #aufbauen(FormLayout)} Felder platzieren und an den {@link #binder} binden</li>
 * </ol>
 *
 * @param <T> Modellklasse des Formulars
 */
public abstract class AbstractFormularView<T> extends VerticalLayout implements HasDynamicTitle {

    protected final BeanValidationBinder<T> binder;

    private final FormularTyp typ;
    private final FormularService service;
    private final Span statusZeile = new Span();

    protected AbstractFormularView(FormularTyp typ, Class<T> modellKlasse, FormularService service) {
        this.typ = typ;
        this.service = service;
        this.binder = new BeanValidationBinder<>(modellKlasse);
    }

    /** Baut die View auf. Muss am Ende des Konstruktors der konkreten View aufgerufen werden. */
    protected final void initialisieren() {
        setPadding(true);
        setMaxWidth("960px");

        add(new H2(typ.getTitel()));
        Paragraph beschreibung = new Paragraph(typ.getBeschreibung());
        beschreibung.addClassNames(LumoUtility.TextColor.SECONDARY);
        add(beschreibung);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2));
        aufbauen(form);
        add(form);

        statusZeile.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontSize.SMALL);
        binder.setStatusLabel(statusZeile);
        add(statusZeile);

        Button absenden = new Button("Absenden", e -> absenden());
        absenden.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button zuruecksetzen = new Button("Zurücksetzen", e -> neuBeginnen());
        zuruecksetzen.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add(new HorizontalLayout(absenden, zuruecksetzen));

        neuBeginnen();
    }

    /** Liefert ein leeres Modell mit ggf. sinnvollen Vorbelegungen. */
    protected abstract T neuesFormular();

    /** Felder ins Layout einfügen und an den Binder binden. */
    protected abstract void aufbauen(FormLayout form);

    /** Hook nach Zurücksetzen/Neubeginn, z. B. um abhängige Felder in den Ausgangszustand zu bringen. */
    protected void nachNeubeginn() {
    }

    @Override
    public String getPageTitle() {
        return typ.getTitel();
    }

    /** Abschnittsüberschrift über die volle Breite des Formulars. */
    protected H4 abschnitt(FormLayout form, String titel) {
        H4 ueberschrift = new H4(titel);
        ueberschrift.addClassName("formular-abschnitt");
        form.add(ueberschrift);
        form.setColspan(ueberschrift, 2);
        return ueberschrift;
    }

    protected void volleBreite(FormLayout form, Component komponente) {
        form.setColspan(komponente, 2);
    }

    /** Standardbindung für Textfelder: leere Eingabe wird als {@code null} ins Modell geschrieben. */
    protected Binder.Binding<T, String> bindeText(TextField feld, String property) {
        return binder.forField(feld).withNullRepresentation("").bind(property);
    }

    private void absenden() {
        statusZeile.setText("");
        T daten = neuesFormular();
        if (!binder.writeBeanIfValid(daten)) {
            zeigeFehler("Bitte prüfen Sie die markierten Felder.");
            return;
        }
        try {
            FormularEinreichung einreichung = service.einreichen(typ, daten);
            Notification erfolg = Notification.show(
                    "Formular eingereicht. Dokument: " + einreichung.dokument().dokumentId()
                            + " (" + einreichung.dokument().status() + ")",
                    6000, Notification.Position.TOP_CENTER);
            erfolg.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            neuBeginnen();
            UI.getCurrent().navigate(EinreichungenView.class);
        } catch (FormularValidierungException e) {
            // Serverseitige Prüfung hat etwas gefunden, das im UI nicht abgefangen wurde
            statusZeile.setText(String.join(" | ", e.getFehler()));
            zeigeFehler("Das Backend hat das Formular abgelehnt.");
        } catch (DokumentErstellungException e) {
            zeigeFehler(e.getMessage());
        }
    }

    private void zeigeFehler(String text) {
        Notification fehler = Notification.show(text, 5000, Notification.Position.TOP_CENTER);
        fehler.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void neuBeginnen() {
        binder.readBean(neuesFormular());
        statusZeile.setText("");
        nachNeubeginn();
    }
}
