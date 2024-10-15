package org.opengpa.frontend.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.security.core.context.SecurityContextHolder;

public class HeaderComponent extends HorizontalLayout {

    public HeaderComponent() {
        setJustifyContentMode(JustifyContentMode.END);
        setWidthFull();
        setClassName("header-component");

        Button avatarButton = new Button(new Icon(VaadinIcon.USER));
        ContextMenu contextMenu = new ContextMenu(avatarButton);
        contextMenu.setOpenOnClick(true);

        contextMenu.addItem("Chat", e -> UI.getCurrent().getPage().setLocation("/"));
        contextMenu.addItem("Documents", e -> UI.getCurrent().getPage().setLocation("/documents"));

        contextMenu.addItem("Log out", event -> {
            UI.getCurrent().getPage().setLocation("/login");
            SecurityContextHolder.clearContext();
        });

        add(avatarButton);
    }
}
