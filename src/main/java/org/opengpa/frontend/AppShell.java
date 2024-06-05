package org.opengpa.frontend;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.TargetElement;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;

@Theme("opengpa")
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("OpenGPA");
        settings.addFavIcon("icon", "icons/favicon-32x32.png", "32x32");
        settings.addLink("shortcut icon", "icons/favicon.ico");
    }
}
