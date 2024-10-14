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
import org.opengpa.rag.action.RagActionChunkResult;
import org.opengpa.rag.action.RagActionResult;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.opengpa.rag.action.RagAction.NAME;

@org.springframework.stereotype.Component
public class RagActionRenderer implements ActionRenderer{
    @Override
    public boolean applies(String action) {
        return NAME.equals(action);
    }

    @Override
    public Optional<Component> render(ActionResult result) {
        RagActionResult ragActionResult = (RagActionResult) result.getResult();
        if (ragActionResult == null) {
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

        var htmlOutput = MarkdownConverter.getInstance().convertToHtml(ragActionResult.getContent());
        var contentDiv = new Html("<div>" +  extractReferences(htmlOutput, ragActionResult.getChunks()) + "</div>");
        contentDiv.setClassName("action-content");
        contentDiv.setVisible(false);

        Button expandButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_DOWN));
        expandButton.addClassName("action-details-button");
        expandButton.addClickListener(e -> contentDiv.setVisible(!contentDiv.isVisible()));

        summaryContainer.add(summaryDiv, expandButton);
        resultContainer.add(summaryContainer, contentDiv);

        return Optional.of(resultContainer);
    }

    private String extractReferences(String content, List<RagActionChunkResult> chunks) {
        // First we extract all UUIDs
        Set<String> uniqueUUIDs = new HashSet<>();
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String uuidString = matcher.group();
            uniqueUUIDs.add(uuidString);
        }

        int index = 0;
        StringBuilder refBuilder = new StringBuilder();
        for (String uuid: uniqueUUIDs) {
            Optional<RagActionChunkResult> chunkResult = chunks.stream().filter(ragActionChunkResult -> ragActionChunkResult.getId().equals(uuid)).findFirst();
            if (chunkResult.isPresent()) {
                content = content.replace(String.format("%s", uuid), String.format("<span class=\"chunk-ref\">%d</span>", index));
                refBuilder.append(String.format("<div class=\"chunk-details\">[<span class=\"chunk-ref\">%d</span>] - %s</div>", index, chunkResult.get().getContent()));
                index++;
            } else {
                content = content.replace(String.format("[%s]", uuid), "");
            }
        }

        refBuilder.insert(0, content);

        return refBuilder.toString();
    }
}
