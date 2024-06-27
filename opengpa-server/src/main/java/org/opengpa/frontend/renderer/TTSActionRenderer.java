package org.opengpa.frontend.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.workspace.Document;

import java.util.List;
import java.util.Optional;

import static org.opengpa.ext.actions.tts.TTSAction.ACTION_NAME;

@org.springframework.stereotype.Component
public class TTSActionRenderer implements ActionRenderer{
    @Override
    public boolean applies(String action) {
        return ACTION_NAME.equals(action);
    }

    @Override
    public Optional<Component> render(ActionResult result) {
        List<Document> documents = result.getDocuments();
        if (documents.isEmpty()) {
            return Optional.empty();
        }

        Document document = documents.getFirst();

        Div resultContainer = new Div();
        resultContainer.setClassName("tts-result-container");
        resultContainer.setWidthFull();

        String url = String.format("/api/files/%s/documents/%s", document.getWorkspaceId(), document.getName());

        // HTML5 audio tag as a String
        String audioHtml = String.format("<audio controls>"
                + "<source src='%s' type='audio/mp3'>"
                + "Your browser does not support the audio element."
                + "</audio>", url);

        Html audio = new Html(audioHtml);

        resultContainer.add(audio);

        return Optional.of(resultContainer);
    }
}
