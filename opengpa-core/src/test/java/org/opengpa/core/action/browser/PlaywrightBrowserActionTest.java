package org.opengpa.core.action.browser;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.config.PlaywrightConfig;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlaywrightBrowserActionTest {

    @Mock
    private PlaywrightConfig playwrightConfig;

    @Mock
    private ChatModel chatModel;

    @Mock
    private Playwright playwright;

    @Mock
    private BrowserType browserType;

    @Mock
    private Browser browser;

    @Mock
    private BrowserContext browserContext;

    @Mock
    private Page page;

    @Mock
    private Agent agent;

    private PlaywrightBrowserAction playwrightBrowserAction;

    @BeforeEach
    void setUp() {
        when(playwrightConfig.isHeadless()).thenReturn(true);
        when(playwrightConfig.getTimeout()).thenReturn(30000l);

        try (MockedStatic<Playwright> playwrightMockedStatic = mockStatic(Playwright.class)) {
            playwrightMockedStatic.when(Playwright::create).thenReturn(playwright);
            when(playwright.chromium()).thenReturn(browserType);
            when(browserType.launch(any())).thenReturn(browser);
            when(browser.newContext(any())).thenReturn(browserContext);

            playwrightBrowserAction = new PlaywrightBrowserAction(playwrightConfig, chatModel);
        }
    }

    @Test
    void testGetName() {
        assertEquals("browse_web", playwrightBrowserAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Read the content at a given url and lookup information in the page.", playwrightBrowserAction.getDescription());
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = playwrightBrowserAction.getParameters();
        assertEquals(2, parameters.size());
        assertEquals("url", parameters.get(0).getName());
        assertEquals("The url of the page to load, must start with http or https.", parameters.get(0).getDescription());
        assertEquals("query", parameters.get(1).getName());
        assertEquals("The question you are looking for in the content, must be a complete question in natural language, it will be used by a LLM agent.", parameters.get(1).getDescription());
    }

    @Test
    void testApplyWithValidInput() {
        String url = "http://example.com";
        String query = "What is this page about?";
        Map<String, Object> input = new HashMap<>();
        input.put("url", url);
        input.put("query", query);

        when(browserContext.newPage()).thenReturn(page);
        when(page.title()).thenReturn("Example Page");
        when(page.content()).thenReturn("<html><body>Example content</body></html>");

        Generation generation = new Generation(new AssistantMessage("This page is about examples."));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        ActionResult result = playwrightBrowserAction.apply(agent, input, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals("Processed webpage Example Page from example.com.", result.getSummary());
        assertEquals("This page is about examples.", result.getResult());
        assertNull(result.getError());

        verify(page).navigate(eq(url), any(Page.NavigateOptions.class));
        verify(page).close();
    }

    @Test
    void testApplyWithMissingUrl() {
        Map<String, Object> input = new HashMap<>();
        input.put("query", "What is this page about?");

        ActionResult result = playwrightBrowserAction.apply(agent, input, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("An error occurred while attempting to browse a site", result.getSummary());
        assertEquals("The url parameter is missing or has an empty value.", result.getError());
    }

    @Test
    void testApplyWithEmptyUrl() {
        Map<String, Object> input = new HashMap<>();
        input.put("url", "");
        input.put("query", "What is this page about?");

        ActionResult result = playwrightBrowserAction.apply(agent, input, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("An error occurred while attempting to browse a site", result.getSummary());
        assertEquals("The url parameter is missing or has an empty value.", result.getError());
    }

    @Test
    void testApplyWithBrowsingException() {
        String url = "http://example.com";
        String query = "What is this page about?";
        Map<String, Object> input = new HashMap<>();
        input.put("url", url);
        input.put("query", query);

        when(browserContext.newPage()).thenReturn(page);
        when(page.navigate(eq(url), any(Page.NavigateOptions.class))).thenThrow(new PlaywrightException("Navigation failed"));

        ActionResult result = playwrightBrowserAction.apply(agent, input, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertEquals("An error occured while attempting to browse http://example.com", result.getSummary());
        assertEquals("Browsing failed, you should try another site.", result.getError());

        verify(page).navigate(eq(url), any(Page.NavigateOptions.class));
    }
}