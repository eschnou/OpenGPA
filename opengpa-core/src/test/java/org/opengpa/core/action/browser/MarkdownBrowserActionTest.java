package org.opengpa.core.action.browser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MarkdownBrowserActionTest {

    @Mock
    private WebClient webClient;

    private MarkdownBrowserAction markdownBrowserAction;

    @BeforeEach
    void setUp() {
        markdownBrowserAction = new MarkdownBrowserAction(webClient);
    }

    @Test
    void testGetName() {
        assertEquals("browse_web", markdownBrowserAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Scrape the content at a given url in markdown format.", markdownBrowserAction.getDescription());
    }

    @Test
    void testFormatResultWithHtmlContent() {
        String url = "http://example.com";
        String htmlContent = "<html><body><h1>Test</h1><p>This is a test.</p></body></html>";

        Map<String, String> result = markdownBrowserAction.formatResult(url, htmlContent);

        assertEquals(url, result.get("url"));
        assertTrue(result.get("content").contains("===="));
        assertTrue(result.get("content").contains("This is a test."));
    }

    @Test
    void testFormatResultWithLongContent() {
        String url = "http://example.com";
        StringBuilder longHtml = new StringBuilder("<html><body>");
        for (int i = 0; i < 1000; i++) {
            longHtml.append("<p>Paragraph ").append(i).append("</p>");
        }
        longHtml.append("</body></html>");

        Map<String, String> result = markdownBrowserAction.formatResult(url, longHtml.toString());

        assertEquals(url, result.get("url"));
        assertEquals(2500, result.get("content").length());
    }

    @Test
    void testFormatResultWithNullContent() {
        String url = "http://example.com";

        Map<String, String> result = markdownBrowserAction.formatResult(url, null);

        assertEquals(url, result.get("url"));
        assertEquals("", result.get("content"));
    }

    @Test
    void testFormatResultWithEmptyContent() {
        String url = "http://example.com";

        Map<String, String> result = markdownBrowserAction.formatResult(url, "");

        assertEquals(url, result.get("url"));
        assertEquals("", result.get("content"));
    }

    @Test
    void testFormatResultWithShortContent() {
        String url = "http://example.com";
        String htmlContent = "<html><body><p>Short content</p></body></html>";

        Map<String, String> result = markdownBrowserAction.formatResult(url, htmlContent);

        assertEquals(url, result.get("url"));
        assertTrue(result.get("content").contains("Short content"));
        assertTrue(result.get("content").length() < 2500);
    }
}