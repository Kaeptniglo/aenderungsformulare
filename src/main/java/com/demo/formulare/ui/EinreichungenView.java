package com.demo.formulare.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.demo.formulare.formular.FormularEinreichung;
import com.demo.formulare.formular.FormularService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Liste aller eingereichten Formulare mit Anzeige der Nutzdaten, die ans Dokumenterstellungs-System gingen.
 */
@Route(value = "einreichungen", layout = MainLayout.class)
@PageTitle("Einreichungen")
public class EinreichungenView extends VerticalLayout {

    private static final DateTimeFormatter ZEIT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final FormularService service;
    private final ObjectMapper objectMapper;
    private final Grid<FormularEinreichung> grid = new Grid<>();
    private final Pre daten = new Pre();
    private final Paragraph hinweis = new Paragraph("Noch keine Einreichungen vorhanden.");

    public EinreichungenView(FormularService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
        setPadding(true);
        setSizeFull();

        add(new H2("Eingereichte Formulare"));
        hinweis.addClassNames(LumoUtility.TextColor.SECONDARY);
        add(hinweis);

        grid.addColumn(e -> ZEIT.format(e.zeitpunkt())).setHeader("Zeitpunkt").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(e -> e.typ().getTitel()).setHeader("Formular").setAutoWidth(true);
        grid.addColumn(e -> e.dokument().dokumentId()).setHeader("Dokument-ID").setAutoWidth(true);
        grid.addColumn(e -> e.dokument().dateiname()).setHeader("Datei").setAutoWidth(true);
        grid.addColumn(e -> e.dokument().status()).setHeader("Status").setAutoWidth(true).setFlexGrow(0);
        grid.setHeight("320px");
        grid.asSingleSelect().addValueChangeListener(e -> zeigeDaten(e.getValue()));
        add(grid);

        Button aktualisieren = new Button("Aktualisieren", VaadinIcon.REFRESH.create(), e -> laden());
        add(aktualisieren);

        add(new H4("Übermittelte Nutzdaten"));
        daten.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.FontSize.SMALL);
        daten.setWidthFull();
        daten.getStyle().set("overflow", "auto").set("white-space", "pre-wrap");
        add(daten);

        laden();
    }

    private void laden() {
        List<FormularEinreichung> liste = service.alleEinreichungen();
        grid.setItems(liste);
        hinweis.setVisible(liste.isEmpty());
        zeigeDaten(null);
    }

    private void zeigeDaten(FormularEinreichung einreichung) {
        if (einreichung == null) {
            daten.setText("(Einreichung in der Liste auswählen)");
            return;
        }
        try {
            daten.setText(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(einreichung.daten()));
        } catch (JacksonException e) {
            daten.setText("Daten konnten nicht dargestellt werden: " + e.getMessage());
        }
    }
}
