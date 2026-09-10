package com.demo.formulare.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.demo.formulare.formular.FormularTyp;

/**
 * Startseite: Auswahl der verfügbaren Formulare.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Übersicht")
public class UebersichtView extends VerticalLayout {

    public UebersichtView() {
        setPadding(true);
        add(new H2("Formular auswählen"));
        add(new Paragraph("Wählen Sie das gewünschte Änderungsformular. Die Eingaben werden geprüft "
                + "und anschließend an das Backend zur Dokumenterstellung übergeben."));

        FlexLayout karten = new FlexLayout();
        karten.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        karten.addClassNames(LumoUtility.Gap.MEDIUM);
        for (FormularTyp typ : FormularTyp.values()) {
            karten.add(karte(typ));
        }
        add(karten);
    }

    private Div karte(FormularTyp typ) {
        Div karte = new Div();
        karte.addClassName("formular-karte");
        karte.setWidth("300px");

        H3 titel = new H3(typ.getTitel());
        titel.addClassNames(LumoUtility.Margin.Top.NONE);
        Paragraph beschreibung = new Paragraph(typ.getBeschreibung());
        beschreibung.addClassNames(LumoUtility.TextColor.SECONDARY);

        Button oeffnen = new Button("Formular öffnen", VaadinIcon.ARROW_RIGHT.create());
        oeffnen.setIconAfterText(true);
        oeffnen.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        oeffnen.addClickListener(e -> UI.getCurrent().navigate(typ.getRoute()));

        karte.add(titel, beschreibung, oeffnen);
        karte.addClickListener(e -> UI.getCurrent().navigate(typ.getRoute()));
        return karte;
    }
}
