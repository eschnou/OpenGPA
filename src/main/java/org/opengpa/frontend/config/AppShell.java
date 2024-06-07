package org.opengpa.frontend.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;

@Theme("opengpa")
@Push()
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("OpenGPA");
        settings.addFavIcon("icon", "icons/favicon-32x32.png", "32x32");
        settings.addLink("shortcut icon", "icons/favicon.ico");
    }
}
