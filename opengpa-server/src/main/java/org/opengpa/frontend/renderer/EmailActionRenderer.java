package org.opengpa.frontend.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.opengpa.core.action.ActionResult;
import org.opengpa.frontend.utils.MarkdownConverter;

import java.util.Map;
import java.util.Optional;

import static org.opengpa.ext.actions.email.SendEmailAction.ACTION_NAME;

@org.springframework.stereotype.Component
public class EmailActionRenderer implements ActionRenderer{
    @Override
    public boolean applies(String action) {
        return ACTION_NAME.equals(action);
    }

    @Override
    public Optional<Component> render(ActionResult result) {
        Map<String, String> actionResult = (Map<String, String>) result.getResult();
        if (actionResult.isEmpty()) {
            return Optional.empty();
        }

        VerticalLayout resultContainer = new VerticalLayout();
        resultContainer.setPadding(false);
        resultContainer.setClassName("action-result-container");

        HorizontalLayout summaryContainer = new HorizontalLayout();
        summaryContainer.setClassName("action-summary-container");
        summaryContainer.setPadding(false);

        Div summaryDiv = new Div(result.getSummary());
        summaryDiv.setClassName("action-summary");

        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setClassName("action-details");
        contentContainer.setVisible(false);

        Button expandButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_DOWN));
        expandButton.addClassName("action-details-button");
        expandButton.addClickListener(e -> contentContainer.setVisible(!contentContainer.isVisible()));

        Html emailTitleDiv = new Html(String.format("<div><b>Subject: </b> %s</div>", actionResult.get("title")));
        emailTitleDiv.setClassName("email-detail-title");

        Div emailContentDiv = new Div(actionResult.get("body"));
        emailContentDiv.setClassName("email-detail-body");

        contentContainer.add(emailTitleDiv, emailContentDiv);

        summaryContainer.add(summaryDiv, expandButton);
        resultContainer.add(summaryContainer, contentContainer);

        return Optional.of(resultContainer);
    }
}
