package org.opengpa.core.action.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BraveSearchActionTest {

    @Mock
    private Agent agent;

    @Mock
    private RestTemplate restTemplate;

    private BraveSearchAction braveSearchAction;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        braveSearchAction = new BraveSearchAction();
        ReflectionTestUtils.setField(braveSearchAction, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(braveSearchAction, "restTemplate", restTemplate);
    }

    @Test
    void testGetName() {
        assertEquals("search_web", braveSearchAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Search the web with Brave Search API.", braveSearchAction.getDescription());
    }

    @Test
    void testGetCategory() {
        assertEquals("web", braveSearchAction.getCategory());
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = braveSearchAction.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("query", parameters.get(0).getName());
        assertEquals("The query to pass to the web search engine.", parameters.get(0).getDescription());
    }

    @Test
    void testApplyWithValidQuery() {
        String query = "test query";
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);

        String mockResponse = """
                {
                  "web": {
                    "results": [
                      {
                        "url": "https://example1.com",
                        "title": "Title 1",
                        "description": "Snippet 1"
                      },
                      {
                        "url": "https://example2.com",
                        "title": "Title 2",
                        "description": "Snippet 2"
                      }
                    ]
                  }
                }
                """;

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        ActionResult result = braveSearchAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertTrue(result.getSummary().contains("Searched the web for \"test query\""));
        assertNull(result.getError());

        List<BraveSearchAction.SearchResult> searchResults = (List<BraveSearchAction.SearchResult>) result.getResult();
        assertEquals(2, searchResults.size());
        assertEquals("https://example1.com", searchResults.get(0).url());
        assertEquals("Title 1", searchResults.get(0).title());
        assertEquals("Snippet 1", searchResults.get(0).snippet());
    }

    @Test
    void testApplyWithEmptyQuery() {
        Map<String, Object> request = new HashMap<>();
        request.put("query", "");

        ActionResult result = braveSearchAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The query parameter is missing or has an empty value.", result.getSummary());
        assertEquals("The query parameter is missing or has an empty value.", result.getError());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testApplyWithMissingQuery() {
        Map<String, Object> request = new HashMap<>();

        ActionResult result = braveSearchAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The query parameter is missing or has an empty value.", result.getSummary());
        assertEquals("The query parameter is missing or has an empty value.", result.getError());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testApplyWithExceptionDuringSearch() {
        String query = "test query";
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API connection failed"));

        ActionResult result = braveSearchAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The web search did not return any results.", result.getSummary());
        assertEquals("API connection failed", result.getError());
    }

    @Test
    void testLongDescriptionIsTruncated() {
        String query = "test query";
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);

        String longDescription = "A".repeat(300);
        String mockResponse = """
                {
                  "web": {
                    "results": [
                      {
                        "url": "https://example.com",
                        "title": "Title",
                        "description": "%s"
                      }
                    ]
                  }
                }
                """.formatted(longDescription);

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        ActionResult result = braveSearchAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        List<BraveSearchAction.SearchResult> searchResults = (List<BraveSearchAction.SearchResult>) result.getResult();
        assertEquals(1, searchResults.size());
        assertEquals(250, searchResults.get(0).snippet().length());
    }

    @Test
    void testEmptyResults() {
        String query = "test query";
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);

        String mockResponse = """
                {
                  "web": {
                    "results": []
                  }
                }
                """;

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        ActionResult result = braveSearchAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        List<BraveSearchAction.SearchResult> searchResults = (List<BraveSearchAction.SearchResult>) result.getResult();
        assertEquals(0, searchResults.size());
    }
}