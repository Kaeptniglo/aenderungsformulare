package com.demo.formulare.api;

import com.demo.formulare.formular.FormularEinreichung;
import com.demo.formulare.formular.FormularService;
import com.demo.formulare.formular.FormularTyp;
import com.demo.formulare.formular.FormularValidierungException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * REST-Schnittstelle zum Backend. Die Vaadin-Views rufen den {@link FormularService} direkt auf
 * (gleiche JVM); diese API ist für andere Clients bzw. für Tests mit curl/Postman gedacht.
 */
@RestController
@RequestMapping("/api")
public class FormularRestController {

    public record FormularInfo(String id, String titel, String beschreibung, String vorlagenId) {
    }

    public record FehlerAntwort(List<String> fehler) {
    }

    private final FormularService service;
    private final ObjectMapper objectMapper;

    public FormularRestController(FormularService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/formulare")
    public List<FormularInfo> formulare() {
        return Arrays.stream(FormularTyp.values())
                .map(t -> new FormularInfo(t.getId(), t.getTitel(), t.getBeschreibung(), t.getVorlagenId()))
                .toList();
    }

    @PostMapping("/formulare/{typId}")
    public ResponseEntity<FormularEinreichung> einreichen(@PathVariable String typId,
                                                          @RequestBody Map<String, Object> body) {
        FormularTyp typ = FormularTyp.vonId(typId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unbekanntes Formular: " + typId));
        Object daten;
        try {
            daten = objectMapper.convertValue(body, typ.getDatenKlasse());
        } catch (IllegalArgumentException e) {
            throw new FormularValidierungException(List.of("Daten nicht lesbar: " + e.getMessage()));
        }
        FormularEinreichung einreichung = service.einreichen(typ, daten);
        return ResponseEntity.status(HttpStatus.CREATED).body(einreichung);
    }

    @GetMapping("/einreichungen")
    public List<FormularEinreichung> einreichungen() {
        return service.alleEinreichungen();
    }

    @GetMapping("/einreichungen/{id}")
    public FormularEinreichung einreichung(@PathVariable String id) {
        try {
            return service.findeEinreichung(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @ExceptionHandler(FormularValidierungException.class)
    public ResponseEntity<FehlerAntwort> validierungFehlgeschlagen(FormularValidierungException e) {
        return ResponseEntity.badRequest().body(new FehlerAntwort(e.getFehler()));
    }
}
