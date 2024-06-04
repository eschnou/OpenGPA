package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.ActionParameter;
import org.opengpa.core.model.ActionResult;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="scrapeurl", havingValue = "true", matchIfMissing = true)
public class ScrapeAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(ReadFileAction.class);

    private static final int MAX_CONTENT_SIZE = 5000;

    private final WebClient webClient;

    public ScrapeAction(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getName() {
        return "scrapeUrl";
    }

    @Override
    public String getDescription() {
        return "Scrape the content at a given url and transform it in a markdown content easy to read.";
    }

    @Override
    public List<ActionParameter> getArguments() {
        return List.of(ActionParameter.from("url", "The url of the content to fetch."));
    }

    public ActionResult apply(Agent agent, Map<String, String> request) {
        log.debug("Scraping url {} for agent {}", request.get("url"), agent.getId());

        String url = request.get("url");

        String content;
        try {
            content = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            return handleWebFetchError(agent, url, String.format("Could not fetch url, error with http status code %d", e.getStatusCode().value()));
        } catch (Exception e) {
            return handleWebFetchError(agent, url, String.format("Could not fetch url, unexpected error."));
        }

        String markdown = convertHtmlToMd(content);

        return ActionResult
                .builder()
                .message(String.format("I have fetched the content at %s", url))
                .status(ActionResult.Status.SUCCESS)
                .output(trimContent(markdown))
                .build();
    }

    private static ActionResult handleWebFetchError(Agent agent, String url, String message) {
        log.error("Failed to scrape url {} for agent {} - {}", url, agent.getId(), message);
        return ActionResult
                .builder()
                .status(ActionResult.Status.FAILURE)
                .message(String.format("Browsing the web for url %s failed.", url))
                .error(message).build();
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