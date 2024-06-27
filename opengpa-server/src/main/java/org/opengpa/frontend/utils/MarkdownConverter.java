package org.opengpa.frontend.utils;

import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

public class MarkdownConverter {

    private static final MarkdownConverter instance = new MarkdownConverter();

    private final Parser parser;

    private final HtmlRenderer renderer;

    private MarkdownConverter() {
        parser = Parser.builder().build();

        renderer = HtmlRenderer.builder()
                .attributeProviderFactory(context -> new CustomAttributeProvider())
                .build();
    }

    public static MarkdownConverter getInstance() {
        return instance;
    }

    public String convertToHtml(String markdown) {
        if (StringUtils.hasText(markdown)) {
            Node document = parser.parse(markdown);
            return renderer.render(document);
        } else {
            return "";
        }
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
