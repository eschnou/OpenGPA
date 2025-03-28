package org.opengpa.core.action.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.LegacyActionAdapter;
import org.opengpa.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="search", havingValue = "brave")
public class BraveSearchAction extends LegacyActionAdapter {

    public static final String ACTION_NAME = "search_web";
    private static final Logger log = LoggerFactory.getLogger(BraveSearchAction.class);
    private static final String BRAVE_SEARCH_URL = "https://api.search.brave.com/res/v1/web/search";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${opengpa.actions.brave.api-key}")
    private String apiKey;

    public record SearchResult(String url, String title, String snippet) {
    }

    public BraveSearchAction() {
        log.info("Creating BraveSearchAction");
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Search the web with Brave Search API.";
    }
    
    @Override
    public String getCategory() {
        return "web";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("query", "The query to pass to the web search engine.")
        );
    }

    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> request, Map<String, String> context) {
        log.debug("Searching web with Brave Search API, query: {}", request.get("query"));

        String query = request.get("query");
        if (query == null || query.isEmpty()) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("The query parameter is missing or has an empty value.")
                    .error("The query parameter is missing or has an empty value.")
                    .build();
        }

        try {
            List<SearchResult> searchResults = performSearch(query);
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .result(searchResults)
                    .summary(String.format("Searched the web for \"%s\" and returned multiple results.", query))
                    .build();
        } catch (Exception e) {
            log.error("Error searching with Brave Search API", e);
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("The web search did not return any results.")
                    .error(e.getMessage())
                    .build();
        }
    }

    private List<SearchResult> performSearch(String query) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("X-Subscription-Token", apiKey);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BRAVE_SEARCH_URL)
                .queryParam("q", query)
                .queryParam("count", 5); // Limit to 5 results

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(), 
                HttpMethod.GET, 
                entity,
                String.class);

        return parseSearchResults(response.getBody());
    }

    private List<SearchResult> parseSearchResults(String responseBody) throws IOException {
        List<SearchResult> resultList = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode webResults = rootNode.path("web").path("results");
        
        if (webResults.isArray()) {
            for (JsonNode result : webResults) {
                String url = result.path("url").asText();
                String title = result.path("title").asText();
                String description = result.path("description").asText();
                
                // Limit snippet length
                String snippet = description.length() > 250 ? description.substring(0, 250) : description;
                
                resultList.add(new SearchResult(url, title, snippet));
            }
        }
        
        return resultList;
    }
}