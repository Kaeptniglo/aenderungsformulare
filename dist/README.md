Fertig gebautes Produktions-Jar (Vaadin production mode, Frontend gebündelt).

Starten: java -jar aenderungsformulare-0.1.0-SNAPSHOT.jar
Voraussetzung: JDK 21 oder neuer. Kein Maven, kein Node.js, kein Internet nötig.

Neu bauen: mvnw.cmd -Dvaadin.productionMode=true package  (Ergebnis liegt in target/)
Zum Bauen reicht ein JDK 21+, Node.js ist nicht nötig, solange das Projekt nur Java-Dateien enthält.
