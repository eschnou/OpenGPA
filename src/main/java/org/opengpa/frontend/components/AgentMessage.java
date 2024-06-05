package org.opengpa.frontend.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AgentMessage extends VerticalLayout {

    private final Div messageDiv;
    private final Button expandButton;
    private final Div detailsDiv;

    public enum Type {
        ACTION,
        OUTPUT
    }

    public AgentMessage(String message, String details, Type type) {
        setClassName("message-container");

        messageDiv = new Div();
        messageDiv.setText(message);
        messageDiv.addClassName("message-summary");

        detailsDiv = new Div();
        detailsDiv.setText("Reasoning: " + details);
        detailsDiv.addClassName("message-details");
        detailsDiv.setVisible(false);

        expandButton = new Button(new Icon(VaadinIcon.LIGHTBULB));
        expandButton.addClassName("message-details-button");
        expandButton.addClickListener(e -> detailsDiv.setVisible(!detailsDiv.isVisible()));

        Div container = new Div(messageDiv, expandButton, detailsDiv);
        container.addClassName(getClassForType(type));

        add(container);
    }

    private String getClassForType(Type type) {
        switch (type) {
            case ACTION:
                return "action-message";
            case OUTPUT:
                return "output-message";
            default:
                return "output-message";
        }
    }
}

