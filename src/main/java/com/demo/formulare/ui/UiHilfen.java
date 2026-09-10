package com.demo.formulare.ui;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.converter.Converter;

import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Kleine Helfer für die Formular-Views.
 */
public final class UiHilfen {

    private UiHilfen() {
    }

    /** Deutsche Beschriftungen und Datumsformat für den DatePicker. */
    public static DatePicker.DatePickerI18n deutschesDatum() {
        return new DatePicker.DatePickerI18n()
                .setMonthNames(List.of("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli",
                        "August", "September", "Oktober", "November", "Dezember"))
                .setWeekdays(List.of("Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"))
                .setWeekdaysShort(List.of("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"))
                .setFirstDayOfWeek(1)
                .setToday("Heute")
                .setCancel("Abbrechen")
                .setDateFormat("dd.MM.yyyy");
    }

    public static DatePicker datum(String label) {
        DatePicker picker = new DatePicker(label);
        picker.setLocale(Locale.GERMANY);
        picker.setI18n(deutschesDatum());
        picker.setPlaceholder("TT.MM.JJJJ");
        return picker;
    }

    /**
     * Konverter für Textfelder: leere Eingabe wird zu {@code null}, ansonsten wird die Eingabe
     * mit {@code normalisierung} bereinigt (z. B. Leerzeichen entfernen, Großschreibung).
     */
    public static Converter<String, String> normalisiert(UnaryOperator<String> normalisierung) {
        return Converter.from(
                eingabe -> Result.ok(eingabe == null || eingabe.isBlank() ? null : normalisierung.apply(eingabe.trim())),
                wert -> wert == null ? "" : wert);
    }

    public static boolean leer(String wert) {
        return wert == null || wert.isBlank();
    }
}
