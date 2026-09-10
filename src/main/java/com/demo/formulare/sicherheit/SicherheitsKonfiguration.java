package com.demo.formulare.sicherheit;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Zugangsschutz per HTTP Basic Auth, gedacht für das Hosting des Prototyps im Internet.
 * <ul>
 *     <li>{@code app.auth.enabled=true}: Oberfläche und REST-API verlangen Benutzername/Passwort
 *         (Browser zeigt den Anmeldedialog, curl: {@code -u benutzer:passwort}).</li>
 *     <li>{@code app.auth.enabled=false} (Standard, lokale Entwicklung): alles frei zugänglich.</li>
 * </ul>
 * {@code /health} bleibt immer offen, damit der Hosting-Dienst die Erreichbarkeit prüfen kann.
 * <p>
 * Später kann hier ein richtiger Login (Vaadin LoginView, OAuth2/OIDC der Firma) ergänzt werden,
 * siehe {@link VaadinSecurityConfigurer#loginView(Class)}.
 */
@Configuration
@EnableWebSecurity
public class SicherheitsKonfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SicherheitsKonfiguration.class);
    private static final String HEALTH_PFAD = "/health";

    @Bean
    @ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true")
    SecurityFilterChain gesicherteKette(HttpSecurity http) throws Exception {
        LOG.info("Zugangsschutz aktiv: HTTP Basic Auth für alle Anfragen außer {}", HEALTH_PFAD);
        http.authorizeHttpRequests(auth -> auth.requestMatchers(HEALTH_PFAD).permitAll())
                // Vaadin-interne Anfragen freigeben, CSRF für Vaadin passend konfigurieren, Rest nur angemeldet.
                // Die Zugriffskontrolle pro View (@PermitAll/@RolesAllowed an den Views) bleibt aus,
                // der Schutz greift komplett auf HTTP-Ebene. Bei Rollen/Login später einschalten.
                .with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin
                        .enableNavigationAccessControl(false)
                        .anyRequest(a -> a.authenticated()))
                .httpBasic(Customizer.withDefaults())
                // REST-API wird ohne Browser-Session aufgerufen (curl/andere Systeme): kein CSRF-Token nötig
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", HEALTH_PFAD));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.auth.enabled", havingValue = "false", matchIfMissing = true)
    SecurityFilterChain offeneKette(HttpSecurity http) throws Exception {
        LOG.info("Zugangsschutz aus (app.auth.enabled=false)");
        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin
                        .enableNavigationAccessControl(false)
                        .anyRequest(a -> a.permitAll()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", HEALTH_PFAD));
        return http.build();
    }

    @Bean
    UserDetailsService benutzerverwaltung(AuthProperties properties) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        if (!properties.enabled()) {
            return manager;
        }
        if (properties.passwort() == null || properties.passwort().isBlank()) {
            throw new IllegalStateException(
                    "app.auth.enabled=true, aber app.auth.passwort (Umgebungsvariable APP_AUTH_PASSWORT) ist leer");
        }
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        manager.createUser(User.withUsername(properties.benutzer())
                .password(encoder.encode(properties.passwort()))
                .roles("USER")
                .build());
        return manager;
    }
}
