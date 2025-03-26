package org.opengpa.core.action.browser;

import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.LegacyActionAdapter;
import org.opengpa.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="browse", havingValue = "raw", matchIfMissing = false)
public class RawBrowserAction extends LegacyActionAdapter {

    private static final Logger log = LoggerFactory.getLogger(RawBrowserAction.class);

    private static final int MAX_CONTENT_SIZE = 1000;

    private final WebClient webClient;

    public RawBrowserAction(WebClient webClient) {
        log.info("Creating RawBrowserAction");
        this.webClient = webClient;
    }

    @Override
    public String getName() {
        return "browse_web";
    }

    @Override
    public String getDescription() {
        return "Fetch the raw html content at a given url.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(ActionParameter.from("url", "The url of the page to load."));
    }
    
    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> request, Map<String, String> context) {
        log.debug("Fetching url {} for agent {}", request.get("url"), agent.getId());

        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("The url parameter is missing or has an empty value.")
                    .error("The url parameter is missing or has an empty value.")
                    .build();
        }

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

        return ActionResult
                .builder()
                .summary(String.format("Reading content at %s", url))
                .status(ActionResult.Status.SUCCESS)
                .result(formatResult(url, content))
                .build();
    }

    protected Map<String, String> formatResult(String url, String content) {
        return Map.of(
                "url", url,
                "content", trimContent(content)
        );
    }

    private String trimContent(String content) {
        if (content == null || content.isEmpty()) return "";
        if (content.length() < MAX_CONTENT_SIZE) return content;
        return content.substring(0, MAX_CONTENT_SIZE);

    }

    private ActionResult handleWebFetchError(Agent agent, String url, String message) {
        log.error("Failed to fetch url {} for agent {} - {}", url, agent.getId(), message);
        return ActionResult
                .builder()
                .status(ActionResult.Status.FAILURE)
                .summary(String.format("Browsing the web for url %s failed.", url))
                .error(message).build();
    }
}