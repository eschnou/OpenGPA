package org.opengpa.frontend.utils;

import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MarkdownConverter {

    private final Parser parser;

    private final HtmlRenderer renderer;

    public MarkdownConverter() {
        parser = Parser.builder().build();

        renderer = HtmlRenderer.builder()
                .attributeProviderFactory(context -> new CustomAttributeProvider())
                .build();
    }

    public String convertToHtml(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    private class CustomAttributeProvider implements AttributeProvider {

        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
            if (node instanceof Link) {
                attributes.put("target", "_blank");
            }
        }

    }
}
