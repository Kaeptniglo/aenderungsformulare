# Änderungsformulare (Prototyp)

Vaadin-Flow-Frontend mit Spring-Boot-Backend, das bisher als PDF vorliegende Änderungsformulare
als Web-Formulare abbildet: Feldvalidierung, gegenseitige Beeinflussung der Felder, gesammelte
Übergabe ans Backend und von dort (per REST) an ein Dokumenterstellungs-System, das die PDFs erzeugt.

## Stack

| Komponente   | Version  |
|--------------|----------|
| Java         | 21 (Ziel), läuft mit JDK 21+ |
| Spring Boot  | 4.1.x (Spring Framework 7, Jackson 3) |
| Vaadin Flow  | 25.2.x   |
| Build        | Maven Wrapper (`mvnw`, keine Maven-Installation nötig) |

Node.js wird von Vaadin beim ersten Start automatisch nach `~/.vaadin` geladen.

Hinweis zur Versionswahl: Die kostenlose Wartung von Vaadin 24 endete im Juni 2026; neuere 24.x-Releases
verlangen im Entwicklungsmodus eine kommerzielle Lizenz. Vaadin 25 ist die aktuelle freie Hauptversion
und setzt Spring Boot 4 voraus.

## Starten

```bash
./mvnw spring-boot:run
```

Windows-Eingabeaufforderung: `mvnw.cmd spring-boot:run`. Danach <http://localhost:8080> öffnen.
Der erste Start dauert länger (Frontend-Bundle wird gebaut). Tests: `./mvnw test`.

Produktions-Build (optimiertes Bundle, ohne Dev-Server): `./mvnw -Dvaadin.productionMode=true package`.
`vaadin-dev` (Dev-Tools, Hot-Reload) ist als `optional` eingebunden und landet nicht im Produktions-Jar.

## In IntelliJ öffnen

`File → Open…` und den Projektordner (bzw. die `pom.xml`) wählen. IntelliJ erkennt den Maven-Wrapper.
Im Ordner `.run/` liegen fertige Run-Konfigurationen, die IntelliJ automatisch anbietet:

| Konfiguration                        | Zweck |
|--------------------------------------|-------|
| `Aenderungsformulare (Spring Boot)`  | App direkt aus der IDE starten (Debuggen möglich, schnellster Weg) |
| `Maven spring-boot:run`              | Start über Maven, identisch zur Kommandozeile |
| `Maven test`                         | Unit-Tests ausführen |
| `Maven package (production)`         | Produktions-Jar mit optimiertem Frontend-Bundle bauen |

Spring Boot DevTools ist eingebunden, Änderungen an Java-Klassen werden nach dem Neubauen (Ctrl+F9)
automatisch übernommen. Beim ersten Start lädt Vaadin Node.js nach `~/.vaadin` und baut das Frontend,
das dauert einige Minuten; danach startet die App in wenigen Sekunden.

## Aufbau

```
src/main/java/com/demo/formulare
├── AenderungsformulareApplication.java   Einstieg, Vaadin-AppShell (lädt Lumo + styles.css per @StyleSheet)
├── model/        Formularmodelle mit Bean-Validation-Regeln (eine Klasse pro Formular)
│   ├── Adressaenderung, Bankverbindungsaenderung, Vertragsaenderung
│   ├── Land, Region                     Wertebereiche für abhängige Auswahlfelder
│   └── IbanPruefer                      IBAN-Prüfung (Mod 97)
├── formular/     Backend-Logik
│   ├── FormularTyp                      Katalog aller Formulare (Titel, Route, Vorlage, Modellklasse)
│   ├── FormularService                  validiert serverseitig, ruft Dokumenterstellung, speichert Einreichung
│   ├── EinreichungRepository            In-Memory-Ablage (später z. B. JPA)
│   └── FormularEinreichung, FormularValidierungException
├── dokument/     Anbindung Dokumenterstellungs-System
│   ├── DokumentErstellungClient         Schnittstelle
│   ├── SimulierterDokumentClient        Standard im Prototyp: loggt Nutzdaten, liefert Fake-ID
│   ├── RestDokumentClient               echter REST-Aufruf (dokumente.api.enabled=true)
│   └── DokumentAnfrage, DokumentAntwort, DokumenteApiProperties
├── api/          REST-Schnittstelle (/api/formulare, /api/einreichungen)
└── ui/           Vaadin-Views
    ├── MainLayout                       Rahmen mit Navigation
    ├── UebersichtView                   Formularauswahl (Route "")
    ├── AbstractFormularView             Basis: Layout, Binder, Absenden → FormularService
    ├── AdressaenderungView, BankverbindungView, VertragsaenderungView
    ├── EinreichungenView                Liste der Einreichungen inkl. gesendeter Nutzdaten
    └── UiHilfen                         DatePicker auf Deutsch, Konverter für Textfelder
```

Eigene Styles liegen in `src/main/resources/META-INF/resources/styles.css` (seit Vaadin 25 sind Themes
normale Stylesheets; das Lumo-Theme und die Lumo-Utility-Klassen werden in der Application-Klasse per
`@StyleSheet` geladen).

### Validierung

* **Feldregeln** stehen als `jakarta.validation`-Annotationen am Modell (`@NotBlank`, `@Pattern`, `@Size`, …).
  Der `BeanValidationBinder` wertet sie im UI aus (inkl. Pflichtfeld-Markierung), der `FormularService`
  prüft sie beim Einreichen erneut serverseitig.
* **Feldübergreifende Regeln** sind im Modell als `@AssertTrue`-Methoden formuliert (z. B.
  `isBicVorhandenWennErforderlich()`), damit das Backend sie unabhängig vom UI prüft. Im UI werden dieselben
  Regeln als Binder-Validatoren direkt am betroffenen Feld gezeigt.

### Gegenseitige Beeinflussung der Felder (Beispiele)

| Formular          | Auslöser                       | Wirkung |
|-------------------|--------------------------------|---------|
| Adressänderung    | Land                           | Bundesland/Kanton-Liste wird gefiltert, PLZ-Format wechselt |
| Adressänderung    | "Abweichende Postanschrift"    | Postanschrift-Felder werden freigeschaltet und Pflicht |
| Bankverbindung    | IBAN-Ländercode ≠ DE           | BIC wird Pflicht |
| Bankverbindung    | "Kontoinhaber ist nicht Kunde" | Beziehung zum Kunden wird Pflicht |
| Vertragsänderung  | Änderungsart                   | Abschnitte Tarifwechsel / Übernahme / Kündigung werden ein-/ausgeblendet |
| Vertragsänderung  | Sparte → Tarif → Laufzeit      | Auswahl wird stufenweise eingeschränkt |
| Vertragsänderung  | Kündigungsgrund "Sonstiges"    | Bemerkung wird Pflicht |

Muster dafür in den Views: `feld.addValueChangeListener(...)` setzt Items/Sichtbarkeit/Pflicht
des abhängigen Feldes und ruft `bindung.validate()` auf, damit Fehlermeldungen sofort aktualisiert werden.

### Datenfluss

1. View füllt per `binder.writeBeanIfValid(...)` das Modell.
2. `FormularService.einreichen(typ, modell)` validiert serverseitig, wandelt das Modell in eine
   `Map<String,Object>` (Feldname → Wert, Enums als stabile Namen wie `DEUTSCHLAND`) und schickt sie
   mit der Vorlagen-ID als `DokumentAnfrage` an den `DokumentErstellungClient`.
3. Die Antwort (Dokument-ID, Dateiname, Status) wird zusammen mit den Nutzdaten als Einreichung abgelegt
   und in der View "Einreichungen" angezeigt.

Da Vaadin Flow serverseitig läuft, ist der Aufruf UI → Backend ein normaler Methodenaufruf im selben
Prozess. Die REST-API unter `/api` bietet denselben Weg für andere Clients:

```bash
curl -X POST http://localhost:8080/api/formulare/adressaenderung -H "Content-Type: application/json" -d "{\"kundennummer\":\"K-123456\",\"vorname\":\"Max\",\"nachname\":\"Mustermann\",\"gueltigAb\":\"2030-01-01\",\"land\":\"DEUTSCHLAND\",\"region\":\"BAYERN\",\"strasse\":\"Marienplatz\",\"hausnummer\":\"1\",\"plz\":\"80331\",\"ort\":\"Muenchen\"}"
```

`GET /api/formulare` listet die Formulare, `GET /api/einreichungen` die Einreichungen.

### Dokumenterstellungs-System anbinden

In `application.properties`:

```properties
dokumente.api.enabled=true
dokumente.api.base-url=http://dokumentsystem:9000
dokumente.api.pfad=/api/dokumente
```

Der `RestDokumentClient` sendet `POST {base-url}{pfad}` mit `DokumentAnfrage` als JSON und erwartet
`DokumentAntwort`. Das Protokoll ist eine Annahme und im Client an das Zielsystem anzupassen.

## Hosting im Internet (Demo von außerhalb erreichbar)

Das Repo enthält alles für einen Container-Deploy:

| Datei | Zweck |
|-------|-------|
| `Dockerfile` | Zweistufig: baut das Produktions-Jar (inkl. Node-Download durch Vaadin), Laufzeit-Image nur mit JRE 21 |
| `render.yaml` | Blueprint für Render.com: Docker-Web-Service in Frankfurt, Health-Check auf `/health`, Zugangsdaten als Umgebungsvariablen |
| `sicherheit/SicherheitsKonfiguration` | HTTP Basic Auth für Oberfläche und REST-API, per `app.auth.enabled` schaltbar |

### Zugangsschutz

Lokal ist der Schutz aus. Beim Hosting per Umgebungsvariablen einschalten:

```
APP_AUTH_ENABLED=true
APP_AUTH_BENUTZER=demo
APP_AUTH_PASSWORT=<geheim>
```

Der Browser fragt dann einmal Benutzername/Passwort ab, die REST-API erwartet `-u benutzer:passwort`.
`/health` bleibt immer offen (für den Health-Check des Hosting-Dienstes). Basic Auth nur über HTTPS
verwenden, was bei Render/Fly automatisch der Fall ist.

### Deploy auf Render (empfohlen, wenige Klicks)

1. Konto auf <https://render.com> anlegen und GitHub verbinden.
2. Dashboard → **New** → **Blueprint** → dieses Repository wählen. Render liest `render.yaml`,
   baut das Dockerfile und erzeugt das Passwort (`APP_AUTH_PASSWORT`) automatisch.
3. Nach dem ersten Build (ca. 5–10 Minuten) ist die App unter `https://aenderungsformulare-<id>.onrender.com`
   erreichbar. Das Passwort steht im Dashboard unter **Environment**.
4. Jeder Push auf `main` löst automatisch einen neuen Deploy aus.

Im kostenlosen Plan schläft der Dienst nach 15 Minuten ohne Zugriff ein, der nächste Aufruf dauert dann
ca. 30–60 Sekunden. Der Plan `starter` (ca. 7 USD/Monat) läuft dauerhaft; dazu in `render.yaml` `plan: starter` setzen.

Alternativen mit derselben Docker-Datei: Fly.io (`fly launch`), Railway, oder ein beliebiger Server mit
`docker build -t aenderungsformulare . && docker run -p 8080:8080 -e APP_AUTH_ENABLED=true -e APP_AUTH_PASSWORT=geheim aenderungsformulare`.

Hinweis: Einreichungen liegen nur im Arbeitsspeicher und sind nach jedem Neustart/Deploy weg.

## Neues Formular hinzufügen

1. Modellklasse in `model/` anlegen (Felder + Validation-Annotationen, feldübergreifende Regeln als `@AssertTrue`).
2. Eintrag in `FormularTyp` ergänzen (ID, Titel, Beschreibung, Vorlagen-ID, Modellklasse).
3. View in `ui/` von `AbstractFormularView<Modell>` ableiten, `@Route(FormularTyp.ROUTE_PRAEFIX + "<id>", layout = MainLayout.class)`,
   in `aufbauen()` Felder platzieren und binden, am Ende des Konstruktors `initialisieren()` aufrufen.

Übersicht und Navigation lesen den `FormularTyp`-Katalog und zeigen das neue Formular automatisch.

## Noch offen (bewusst nicht Teil des Prototyps)

* Richtige Anmeldung/Benutzerverwaltung (aktuell nur HTTP Basic Auth mit einem Benutzer; Vaadins
  View-Zugriffskontrolle per `@PermitAll`/`@RolesAllowed` ist in `SicherheitsKonfiguration` abgeschaltet)
* Persistenz der Einreichungen (Datenbank statt In-Memory)
* Echtes Protokoll des Dokumenterstellungs-Systems, Download/Anzeige der erzeugten PDFs
* Die drei Beispielformulare durch die realen PDF-Formulare ersetzen
