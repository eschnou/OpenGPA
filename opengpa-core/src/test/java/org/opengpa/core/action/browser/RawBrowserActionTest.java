package org.opengpa.core.action.browser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RawBrowserActionTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Agent agent;

    private RawBrowserAction rawBrowserAction;

    @BeforeEach
    void setUp() {
        rawBrowserAction = new RawBrowserAction(webClient);

        // Setup the WebClient mock chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void testGetName() {
        assertEquals("browse_web", rawBrowserAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Fetch the raw html content at a given url.", rawBrowserAction.getDescription());
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = rawBrowserAction.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("url", parameters.get(0).getName());
        assertEquals("The url of the page to load.", parameters.get(0).getDescription());
    }

    @Test
    void testApplyWithValidUrl() {
        String url = "http://example.com";
        String content = "<html><body>Example content</body></html>";
        Map<String, String> request = new HashMap<>();
        request.put("url", url);

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(content));

        ActionResult result = rawBrowserAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals("Reading content at http://example.com", result.getSummary());
        assertNull(result.getError());
        assertEquals(url,((Map<String, String>) result.getResult()).get("url"));
        assertEquals(content, ((Map<String, String>) result.getResult()).get("content"));
    }

    @Test
    void testApplyWithMissingUrl() {
        Map<String, String> request = new HashMap<>();

        ActionResult result = rawBrowserAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The url parameter is missing or has an empty value.", result.getSummary());
        assertEquals("The url parameter is missing or has an empty value.", result.getError());
    }

    @Test
    void testApplyWithEmptyUrl() {
        Map<String, String> request = new HashMap<>();
        request.put("url", "");

        ActionResult result = rawBrowserAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("The url parameter is missing or has an empty value.", result.getSummary());
        assertEquals("The url parameter is missing or has an empty value.", result.getError());
    }

    @Test
    void testApplyWithWebClientResponseException() {
        String url = "http://example.com";
        Map<String, String> request = new HashMap<>();
        request.put("url", url);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new WebClientResponseException(404, "Not Found", null, null, null)));

        ActionResult result = rawBrowserAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("Browsing the web for url http://example.com failed.", result.getSummary());
        assertEquals("Could not fetch url, error with http status code 404", result.getError());
    }

    @Test
    void testApplyWithUnexpectedException() {
        String url = "http://example.com";
        Map<String, String> request = new HashMap<>();
        request.put("url", url);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Unexpected error")));

        ActionResult result = rawBrowserAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("Browsing the web for url http://example.com failed.", result.getSummary());
        assertEquals("Could not fetch url, unexpected error.", result.getError());
    }

    @Test
    void testFormatResultWithShortContent() {
        String url = "http://example.com";
        String content = "Short content";

        Map<String, String> result = rawBrowserAction.formatResult(url, content);

        assertEquals(url, result.get("url"));
        assertEquals(content, result.get("content"));
    }

    @Test
    void testFormatResultWithLongContent() {
        String url = "http://example.com";
        String content = "a".repeat(2000);

        Map<String, String> result = rawBrowserAction.formatResult(url, content);

        assertEquals(url, result.get("url"));
        assertEquals(1000, result.get("content").length());
        assertEquals("a".repeat(1000), result.get("content"));
    }

    @Test
    void testFormatResultWithNullContent() {
        String url = "http://example.com";

        Map<String, String> result = rawBrowserAction.formatResult(url, null);

        assertEquals(url, result.get("url"));
        assertEquals("", result.get("content"));
    }

    @Test
    void testFormatResultWithEmptyContent() {
        String url = "http://example.com";

        Map<String, String> result = rawBrowserAction.formatResult(url, "");

        assertEquals(url, result.get("url"));
        assertEquals("", result.get("content"));
    }
}