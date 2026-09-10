package com.demo.formulare.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.demo.formulare.formular.FormularTyp;

/**
 * Rahmenlayout mit Kopfzeile und seitlicher Navigation. Alle Views hängen sich hier ein.
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 titel = new H1("Änderungsformulare");
        titel.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        addToNavbar(new DrawerToggle(), titel);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Übersicht", UebersichtView.class, VaadinIcon.HOME.create()));

        SideNavItem formulare = new SideNavItem("Formulare");
        formulare.setPrefixComponent(VaadinIcon.FORM.create());
        for (FormularTyp typ : FormularTyp.values()) {
            formulare.addItem(new SideNavItem(typ.getTitel(), typ.getRoute(), VaadinIcon.FILE_TEXT_O.create()));
        }
        formulare.setExpanded(true);
        nav.addItem(formulare);

        nav.addItem(new SideNavItem("Einreichungen", EinreichungenView.class, VaadinIcon.LIST.create()));

        Scroller scroller = new Scroller(nav);
        scroller.addClassNames(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }
}
