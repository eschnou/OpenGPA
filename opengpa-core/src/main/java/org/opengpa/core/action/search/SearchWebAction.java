package org.opengpa.core.action.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.LegacyActionAdapter;
import org.opengpa.core.action.files.ReadFileAction;
import org.opengpa.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="search", havingValue = "duckduckgo", matchIfMissing = true)
public class SearchWebAction extends LegacyActionAdapter {

    public static final String ACTION_NAME = "search_web";

    private static final Logger log = LoggerFactory.getLogger(ReadFileAction.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static String DUCKDUCKGO_SEARCH_URL = "https://duckduckgo.com/html/?q=";

    public record SearchResult(String url, String title, String snippet) {
    }

    public SearchWebAction() {
        log.info("Creating SearchWebAction");
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Search the web with the given query.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("query", "The query to pass to the web search engine.")
        );
    }

    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> request,  Map<String, String> context) {
        log.debug("Searching web with query {}", request.get("query"));

        String query = request.get("query");
        if (query == null || query.isEmpty()) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("The query parameter is missing or has an empty value.")
                    .error("The query parameter is missing or has an empty value.")
                    .build();
        }

        try {
            List<SearchResult> searchResults = getSearchResults(query);
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .result(searchResults)
                    .summary(String.format("Searched the web for \"%s\" and returned multiple results.", query))
                    .build();
        } catch (Exception e) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("The web search did not return any results.")
                    .error(e.getMessage())
                    .build();
        }
    }

    public List<SearchResult> getSearchResults(String query) throws IOException {
        Elements results = fetchSearchResults(query);
        List<SearchResult> resultList = new ArrayList<>();

        for (Element result : results) {
            Element title = result.getElementsByClass("links_main").first().getElementsByTag("a").first();
            Element snippet = result.getElementsByClass("result__snippet").first();
            String snippetText = snippet.text().substring(0, snippet.text().length() > 250 ? 250 : snippet.text().length());

            resultList.add(new SearchResult(title.attr("href"), title.text(), snippetText));
        }

        return resultList.subList(0, resultList.size() > 5 ? 5 : resultList.size());
    }

    @VisibleForTesting
    Elements fetchSearchResults(String query) throws IOException {
        Document doc = Jsoup.connect(DUCKDUCKGO_SEARCH_URL + query).get();
        Elements results = doc.getElementById("links").getElementsByClass("results_links");
        return results;
    }
}