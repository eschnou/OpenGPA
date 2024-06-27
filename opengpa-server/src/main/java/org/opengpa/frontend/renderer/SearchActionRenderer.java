package org.opengpa.frontend.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.shared.TooltipConfiguration;
import org.opengpa.core.action.ActionResult;

import java.util.List;
import java.util.Optional;

import static org.opengpa.core.action.search.SearchWebAction.ACTION_NAME;
import static org.opengpa.core.action.search.SearchWebAction.SearchResult;

@org.springframework.stereotype.Component
public class SearchActionRenderer implements ActionRenderer{
    @Override
    public boolean applies(String action) {
        return ACTION_NAME.equals(action);
    }

    @Override
    public Optional<Component> render(ActionResult result) {
        List<SearchResult> searchResults = (List<SearchResult>) result.getResult();
        if (searchResults.isEmpty()) {
            return Optional.empty();
        }

        VerticalLayout resultContainer = new VerticalLayout();
        resultContainer.setClassName("search-result-container");

        Div searchSummaryDiv = new Div(result.getSummary());
        searchSummaryDiv.setClassName("search-summary");
        resultContainer.add(searchSummaryDiv);

        for (SearchResult searchResult : searchResults) {
            Anchor anchor = new Anchor(searchResult.url(), searchResult.title(), AnchorTarget.BLANK);
            anchor.setClassName("search-result");

            Tooltip tooltip = Tooltip.forComponent(anchor);
            tooltip.setPosition(Tooltip.TooltipPosition.TOP_END);
            tooltip.setText(searchResult.snippet());

            resultContainer.add(anchor);
        }

        return Optional.of(resultContainer);
    }
}
