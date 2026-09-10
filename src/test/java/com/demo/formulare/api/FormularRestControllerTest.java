package com.demo.formulare.api;

import com.demo.formulare.dokument.SimulierterDokumentClient;
import com.demo.formulare.formular.EinreichungRepository;
import com.demo.formulare.formular.FormularService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FormularRestControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper mapper = JsonMapper.builder().build();
        FormularService service = new FormularService(
                Validation.buildDefaultValidatorFactory().getValidator(),
                mapper,
                new SimulierterDokumentClient(mapper),
                new EinreichungRepository());
        mockMvc = MockMvcBuilders.standaloneSetup(new FormularRestController(service, mapper))
                .setMessageConverters(new JacksonJsonHttpMessageConverter(mapper))
                .build();
    }

    @Test
    void listetFormulare() throws Exception {
        mockMvc.perform(get("/api/formulare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value("adressaenderung"));
    }

    @Test
    void reichtGueltigesFormularEin() throws Exception {
        String json = """
                {
                  "kundennummer": "K-123456",
                  "vorname": "Max",
                  "nachname": "Mustermann",
                  "gueltigAb": "%s",
                  "land": "DEUTSCHLAND",
                  "region": "BAYERN",
                  "strasse": "Marienplatz",
                  "hausnummer": "1",
                  "plz": "80331",
                  "ort": "München"
                }
                """.formatted(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/formulare/adressaenderung").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typ").value("ADRESSAENDERUNG"))
                .andExpect(jsonPath("$.dokument.dokumentId").value(startsWith("SIM-")));

        mockMvc.perform(get("/api/einreichungen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void lehntUngueltigesFormularAb() throws Exception {
        String json = """
                {"kundennummer": "falsch", "land": "SCHWEIZ", "region": "BAYERN"}
                """;

        mockMvc.perform(post("/api/formulare/adressaenderung").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fehler").isArray())
                .andExpect(jsonPath("$.fehler[?(@ =~ /.*Format: K-123456.*/)]").exists())
                .andExpect(jsonPath("$.fehler[?(@ =~ /.*passt nicht zum gewählten Land.*/)]").exists());
    }

    @Test
    void unbekanntesFormularLiefert404() throws Exception {
        mockMvc.perform(post("/api/formulare/gibtesnicht").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }
}
