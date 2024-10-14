package org.opengpa.core.action.browser;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="browse", havingValue = "markdown", matchIfMissing = false)
public class MarkdownBrowserAction extends RawBrowserAction {

    private static final Logger log = LoggerFactory.getLogger(MarkdownBrowserAction.class);

    private static final int MAX_CONTENT_SIZE = 2500;

    public MarkdownBrowserAction(WebClient webClient)  {
        super(webClient);
        log.info("Creating MarkdownBrowserAction");
    }

    @Override
    public String getDescription() {
        return "Scrape the content at a given url in markdown format.";
    }

    protected Map<String, String> formatResult(String url, String content) {
        return Map.of(
                "url", url,
                "content", trimContent(convertHtmlToMd(content))
        );
    }

    private String convertHtmlToMd(String html) {
        if (html == null || html.isEmpty()) return "";

        Document document = Jsoup.parse(html);
        String markdown = FlexmarkHtmlConverter.builder().build().convert(document.html());
        return markdown;
    }

    private String trimContent(String content) {
        if (content == null || content.isEmpty()) return "";
        if (content.length() < MAX_CONTENT_SIZE) return content;
        return content.substring(0, MAX_CONTENT_SIZE);

    }
}