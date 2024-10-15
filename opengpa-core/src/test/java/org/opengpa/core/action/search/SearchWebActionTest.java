package org.opengpa.core.action.search;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchWebActionTest {

    @Mock
    private Agent agent;

    private SearchWebAction searchWebAction;

    @BeforeEach
    void setUp() {
        searchWebAction = new SearchWebAction() {
            @Override
            Elements fetchSearchResults(String query) throws IOException {
                // This will be overridden in specific tests
                return new Elements();
            }
        };
    }

    @Test
    void testGetName() {
        assertEquals("search_web", searchWebAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Search the web with the given query.", searchWebAction.getDescription());
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = searchWebAction.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("query", parameters.get(0).getName());
        assertEquals("The query to pass to the web search engine.", parameters.get(0).getDescription());
    }

    @Test
    void testApplyWithValidQuery() throws IOException {
        String query = "test query";
        Map<String, String> request = new HashMap<>();
        request.put("query", query);

        searchWebAction = new SearchWebAction() {
            @Override
            Elements fetchSearchResults(String query) {
                Elements mockResults = new Elements();
                mockResults.add(createMockResult("http://example1.com", "Title 1", "Snippet 1"));
                mockResults.add(createMockResult("http://example2.com", "Title 2", "Snippet 2"));
                return mockResults;
            }
        };

        ActionResult result = searchWebAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertTrue(result.getSummary().contains("Searched the web for \"test query\""));
        assertNull(result.getError());

        List<SearchWebAction.SearchResult> searchResults = (List<SearchWebAction.SearchResult>) result.getResult();
        assertEquals(2, searchResults.size());
        assertEquals("http://example1.com", searchResults.get(0).url());
        assertEquals("Title 1", searchResults.get(0).title());
        assertEquals("Snippet 1", searchResults.get(0).snippet());
    }

    @Test
    void testApplyWithEmptyQuery() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "");

        ActionResult result = searchWebAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The query parameter is missing or has an empty value.", result.getSummary());
        assertEquals("The query parameter is missing or has an empty value.", result.getError());
    }

    @Test
    void testApplyWithMissingQuery() {
        Map<String, String> request = new HashMap<>();

        ActionResult result = searchWebAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The query parameter is missing or has an empty value.", result.getSummary());
        assertEquals("The query parameter is missing or has an empty value.", result.getError());
    }

    @Test
    void testApplyWithExceptionDuringSearch() {
        String query = "test query";
        Map<String, String> request = new HashMap<>();
        request.put("query", query);

        searchWebAction = new SearchWebAction() {
            @Override
            Elements fetchSearchResults(String query) throws IOException {
                throw new IOException("Connection failed");
            }
        };

        ActionResult result = searchWebAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The web search did not return any results.", result.getSummary());
        assertEquals("Connection failed", result.getError());
    }

    @Test
    void testGetSearchResultsLimitTo5() throws IOException {
        searchWebAction = new SearchWebAction() {
            @Override
            Elements fetchSearchResults(String query) {
                Elements mockResults = new Elements();
                for (int i = 1; i <= 6; i++) {
                    mockResults.add(createMockResult("http://example" + i + ".com", "Title " + i, "Snippet " + i));
                }
                return mockResults;
            }
        };

        List<SearchWebAction.SearchResult> results = searchWebAction.getSearchResults("test query");

        assertEquals(5, results.size());
        assertEquals("http://example1.com", results.get(0).url());
        assertEquals("http://example5.com", results.get(4).url());
    }

    @Test
    void testGetSearchResultsWithLongSnippet() throws IOException {
        searchWebAction = new SearchWebAction() {
            @Override
            Elements fetchSearchResults(String query) {
                Elements mockResults = new Elements();
                mockResults.add(createMockResult("http://example.com", "Title", "A".repeat(300)));
                return mockResults;
            }
        };

        List<SearchWebAction.SearchResult> results = searchWebAction.getSearchResults("test query");

        assertEquals(1, results.size());
        assertEquals(250, results.get(0).snippet().length());
    }

    private Element createMockResult(String url, String title, String snippet) {
        Element result = mock(Element.class);
        Element linksMain = mock(Element.class);
        Element titleElement = mock(Element.class);
        Element snippetElement = mock(Element.class);

        when(result.getElementsByClass("links_main")).thenReturn(new Elements(linksMain));
        when(linksMain.getElementsByTag("a")).thenReturn(new Elements(titleElement));
        when(titleElement.attr("href")).thenReturn(url);
        when(titleElement.text()).thenReturn(title);
        when(result.getElementsByClass("result__snippet")).thenReturn(new Elements(snippetElement));
        when(snippetElement.text()).thenReturn(snippet);

        return result;
    }
}