package com.demo.formulare.sicherheit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Einstellungen für den Zugangsschutz (Präfix {@code app.auth}).
 *
 * @param enabled  {@code true}: alle Seiten und die REST-API verlangen HTTP Basic Auth
 * @param benutzer Benutzername
 * @param passwort Passwort im Klartext (kommt beim Hosting aus einer Umgebungsvariable)
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(boolean enabled, String benutzer, String passwort) {
}
