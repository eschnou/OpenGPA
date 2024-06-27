package org.opengpa.frontend.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.opengpa.frontend.config.UIConfig;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final UIConfig uiConfig;

    public LoginView(UIConfig uiConfig) {
        this.uiConfig = uiConfig;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        var login = new LoginForm();
        login.setAction("login");
        login.setForgotPasswordButtonVisible(false);

        add(
                new H1(uiConfig.getName()),
                login
        );
    }
}
