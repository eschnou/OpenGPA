package org.opengpa.core.action;

import org.opengpa.core.model.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.ActionParameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="websearch", havingValue = "true", matchIfMissing = true)
public class WebSearchAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(ReadFileAction.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static String DUCKDUCKGO_SEARCH_URL = "https://duckduckgo.com/html/?q=";

    public record SearchResult(String url, String title, String snippet) {
    }

    @Override
    public String getName() {
        return "webSearch";
    }

    @Override
    public String getDescription() {
        return "Search the web with the given query.";
    }

    @Override
    public List<ActionParameter> getArguments() {
        return List.of(
                ActionParameter.from("query", "The query to pass to the web search engine.")
        );
    }

    public ActionResult apply(Agent agent, Map<String, String> request) {
        log.debug("Searching web with query {}", request.get("query"));

        String query = request.get("query");
        try {
            List<SearchResult> searchResults = getSearchResults(query);
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .output(formatResult(searchResults))
                    .message(String.format("Searching for \"%s\" returned multiple results.", query))
                    .build();
        } catch (Exception e) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .message("The web search did not return any results.")
                    .error(e.getMessage())
                    .build();
        }
    }

    public List<SearchResult> getSearchResults(String query) throws IOException {
        Document doc = Jsoup.connect(DUCKDUCKGO_SEARCH_URL + query).get();
        Elements results = doc.getElementById("links").getElementsByClass("results_links");
        List<SearchResult> resultList = new ArrayList<>();

        for (Element result : results) {
            Element title = result.getElementsByClass("links_main").first().getElementsByTag("a").first();
            Element snippet = result.getElementsByClass("result__snippet").first();
            String snippetText = snippet.text().substring(0, snippet.text().length() > 250 ? 250 : snippet.text().length());

            resultList.add(new SearchResult(title.attr("href"), title.text(), snippetText));
        }

        return resultList.subList(0, resultList.size() > 5 ? 5 : resultList.size());
    }

    private String formatResult(List<SearchResult> list) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}