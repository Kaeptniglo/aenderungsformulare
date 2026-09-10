package com.demo.formulare.formular;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Einfache In-Memory-Ablage für den Prototyp. Kann später durch Spring Data (JPA o. ä.) ersetzt werden.
 */
@Repository
public class EinreichungRepository {

    private final Map<String, FormularEinreichung> ablage = new ConcurrentHashMap<>();

    public FormularEinreichung speichern(FormularEinreichung einreichung) {
        ablage.put(einreichung.id(), einreichung);
        return einreichung;
    }

    public Optional<FormularEinreichung> finde(String id) {
        return Optional.ofNullable(ablage.get(id));
    }

    /** Alle Einreichungen, neueste zuerst. */
    public List<FormularEinreichung> alle() {
        List<FormularEinreichung> liste = new ArrayList<>(ablage.values());
        liste.sort(Comparator.comparing(FormularEinreichung::zeitpunkt).reversed());
        return liste;
    }
}
