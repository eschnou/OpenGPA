package org.opengpa.frontend.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AgentMessage extends VerticalLayout {

    public enum Type {
        ACTION,
        OUTPUT
    }

    public AgentMessage(String message, String details, Type type) {
        setClassName("message-container");

        Component messageDiv = getMessageComponent(type, message);
        HtmlComponent detailsDiv = getDetailsComponent(details);

        Button expandButton = new Button(new Icon(VaadinIcon.LIGHTBULB));
        expandButton.addClassName("message-details-button");
        expandButton.addClickListener(e -> detailsDiv.setVisible(!detailsDiv.isVisible()));

        Div container = new Div(messageDiv, expandButton, detailsDiv);
        container.addClassName(getClassForType(type));

        add(container);
    }

    private Component getMessageComponent(Type type, String message) {
        switch (type) {
            case ACTION:
                return getMessageDiv(message);
            case OUTPUT:
                return getMessageHtml(message);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private Div getMessageDiv(String textMessage) {
        Div messageDiv = new Div();
        messageDiv.setText(textMessage);
        messageDiv.addClassName("message-summary");
        return messageDiv;
    }

    private Html getMessageHtml(String htmlMessage) {
        Html messageHtml = new Html("<div>" + htmlMessage + "</div>");
        messageHtml.addClassName("message-summary");
        return messageHtml;
    }

    private HtmlComponent getDetailsComponent(String details) {
        Div detailsDiv = new Div();
        detailsDiv.setText("Reasoning: " + details);
        detailsDiv.addClassName("message-details");
        detailsDiv.setVisible(false);
        return detailsDiv;
    }

    private String getClassForType(Type type) {
        switch (type) {
            case ACTION:
                return "action-message";
            case OUTPUT:
                return "output-message";
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}

