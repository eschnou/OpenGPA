package org.opengpa.core.action.browser;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.config.PlaywrightConfig;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "opengpa.actions", name = "browse", havingValue = "playwright", matchIfMissing = false)
public class PlaywrightBrowserAction implements Action {

    public static final String BROWSER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (K HTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";

    private static final String PROMPT = """
            The following is a web page content. Using this content, try to answer the question below. If you cannot answer the question, explain
            what information is missing and do provide a short summary of what the page is about. If you find links that might contain
            further details, list the links and a brief explanation for each link.
            
            Question: %s
            Page title: %s
            Content:
           
            """;
    public static final String NAME = "browse_web";

    private final PlaywrightConfig playwrightConfig;
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext browserContext;
    private final ChatModel chatModel;

    public PlaywrightBrowserAction(PlaywrightConfig playwrightConfig, ChatModel chatModel) {
        this.playwrightConfig = playwrightConfig;
        this.chatModel = chatModel;
        this.playwright = Playwright.create();

        BrowserType browserType = playwright.chromium();
        this.browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(playwrightConfig.isHeadless()));

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        contextOptions.setUserAgent(BROWSER_AGENT);

        this.browserContext = browser.newContext(contextOptions);
        log.info("Creating PlaywrightBrowserAction");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Read the content at a given url and lookup information in the page.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("url", "The url of the page to load, must start with http or https."),
                ActionParameter.from("query", "The question you are looking for in the content, must be a complete question in natural language, it will be used by a LLM agent.")
        );
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, String> input,  Map<String, String> context) {
        log.debug("Fetching url {} for agent {}", input.get("url"), agent.getId());

        String url = input.get("url");
        if (url == null || url.isEmpty()) {
            return errorResult("An error occurred while attempting to browse a site", "The url parameter is missing or has an empty value.");
        }

        try {
            return browsePage(url, input.get("query"));
        } catch (Exception e) {
            log.warn("Browsing error for {}", url, e);
            return errorResult(
                    String.format("An error occured while attempting to browse %s", url),
                    "Browsing failed, you should try another site.");
        }
    }

    private ActionResult browsePage(String url, String query) {
        Page page = browserContext.newPage();
        Page.NavigateOptions navigateOptions = new Page.NavigateOptions();
        navigateOptions.setTimeout(playwrightConfig.getTimeout());
        navigateOptions.setWaitUntil(WaitUntilState.DOMCONTENTLOADED);

        page.navigate(url, navigateOptions);
        String title = page.title();
        String content = page.content();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(PROMPT, query, title));
        stringBuilder.append(convertHtmlToMd(content));

        String prompt = stringBuilder.toString();
        Generation response = chatModel.call(new Prompt(prompt)).getResult();

        page.close();

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .result(response.getOutput().getContent())
                .summary(String.format("Processed webpage '" +  title + "' from " + getHostFromUrl(url)))
                .build();
    }

    private static ActionResult errorResult(String userMessage, String errorMessage) {
        return ActionResult.builder()
                .status(ActionResult.Status.FAILURE)
                .summary(userMessage)
                .error(errorMessage)
                .build();
    }

    private String convertHtmlToMd(String html) {
        if (html == null || html.isEmpty()) return "";

        Document document = Jsoup.parse(html);
        String markdown = FlexmarkHtmlConverter.builder().build().convert(document.html());
        return markdown;
    }

    private String getHostFromUrl(String urlString) {
        try {
            return new URL(urlString).getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

}
