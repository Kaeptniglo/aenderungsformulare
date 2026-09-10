package com.demo.formulare;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Einstiegspunkt der Anwendung. Implementiert {@link AppShellConfigurator}, damit
 * Vaadin-Shell-Einstellungen (Stylesheets, PWA, Push, ...) zentral hier konfiguriert werden können.
 * <p>
 * Seit Vaadin 25 sind Themes normale Stylesheets: Lumo, die Lumo-Utility-Klassen und das eigene
 * Stylesheet ({@code src/main/resources/META-INF/resources/styles.css}) werden per {@link StyleSheet} geladen.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css")
public class AenderungsformulareApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(AenderungsformulareApplication.class, args);
    }
}
