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
import org.springframework.util.StringUtils;

import java.util.Optional;

import static org.opengpa.core.action.browser.PlaywrightBrowserAction.NAME;

@org.springframework.stereotype.Component
public class BrowseActionRenderer implements ActionRenderer{
    @Override
    public boolean applies(String action) {
        return NAME.equals(action);
    }

    @Override
    public Optional<Component> render(ActionResult result) {
        String browseResult = (String) result.getResult();
        if (browseResult.isEmpty()) {
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

        var htmlOutput = MarkdownConverter.getInstance().convertToHtml(browseResult);
        var contentDiv = new Html("<div>" +  htmlOutput + "</div>");
        contentDiv.setClassName("action-content");
        contentDiv.setVisible(false);

        Button expandButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_DOWN));
        expandButton.addClassName("action-details-button");
        expandButton.addClickListener(e -> contentDiv.setVisible(!contentDiv.isVisible()));

        summaryContainer.add(summaryDiv, expandButton);
        resultContainer.add(summaryContainer, contentDiv);

        return Optional.of(resultContainer);
    }
}
