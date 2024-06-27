package org.opengpa.core.agent.react;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.browser.RawBrowserAction;
import org.opengpa.core.action.search.SearchWebAction;
import org.opengpa.core.agent.ActionInvocation;
import org.opengpa.core.agent.AgentStep;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ReActAgentTest {

    @Test
    void renderTools() {
        List<Action> actions = Arrays.asList(
                new RawBrowserAction(null),
                new SearchWebAction()
        );

        ReActAgent reActAgent = new ReActAgent(null, null, null, "Task", new HashMap<>());
        String result = reActAgent.renderTools(actions);
        Assertions.assertEquals("[ {\n  \"name\" : \"browse_web\",\n  \"description\" : \"Fetch the raw html content at a given url.\",\n  \"parameters\" : [ {\n    \"name\" : \"url\",\n    \"description\" : \"The url of the page to load.\"\n  } ]\n}, {\n  \"name\" : \"search_web\",\n  \"description\" : \"Search the web with the given query.\",\n  \"parameters\" : [ {\n    \"name\" : \"query\",\n    \"description\" : \"The query to pass to the web search engine.\"\n  } ]\n} ]", result);
    }

    @Test
    void renderContext() {
        ReActAgent reActAgent = new ReActAgent(null, null, null, "Task", new HashMap<>());
        String result = reActAgent.renderContext(Map.of(
                "username", "johndoe"
        ));
        Assertions.assertEquals("{\n  \"username\" : \"johndoe\"\n}", result);
    }

    @Test
    void renderSteps() {
        ReActAgent reActAgent = new ReActAgent(null, null, null, "Task", new HashMap<>());
        List<AgentStep> steps = Arrays.asList(
            AgentStep.builder()
                    .input("Task 1")
                    .reasoning("Reason for doing this action for 1")
                    .isFinal(false)
                    .action(ActionInvocation.builder().name("web_search").parameters(Map.of("query", "what is the weather")).build())
                    .result(ActionResult.builder().status(ActionResult.Status.SUCCESS).summary("Query returned two links").result(Arrays.asList("link1", "link2")).build())
                    .build(),
                AgentStep.builder()
                        .input("Task 2")
                        .reasoning("Reason for doing this action for 2")
                        .isFinal(true)
                        .action(ActionInvocation.builder().name("scrape_url").parameters(Map.of("query", "http://url")).build())
                        .result(ActionResult.builder().status(ActionResult.Status.FAILURE)
                                .summary("Couldn't read the page")
                                .result(Map.of("key", "value"))
                                .error("Could not read the page").build())
                        .build()
        );

        String result = reActAgent.renderPreviousSteps(steps);
        Assertions.assertEquals("[ {\n  \"input\" : \"Task 1\",\n  \"reasoning\" : \"Reason for doing this action for 1\",\n  \"action\" : {\n    \"name\" : \"web_search\",\n    \"parameters\" : {\n      \"query\" : \"what is the weather\"\n    }\n  },\n  \"final\" : false,\n  \"result\" : {\n    \"status\" : \"SUCCESS\",\n    \"summary\" : \"Query returned two links\",\n    \"result\" : [ \"link1\", \"link2\" ]\n  }\n}, {\n  \"input\" : \"Task 2\",\n  \"reasoning\" : \"Reason for doing this action for 2\",\n  \"action\" : {\n    \"name\" : \"scrape_url\",\n    \"parameters\" : {\n      \"query\" : \"http://url\"\n    }\n  },\n  \"final\" : true,\n  \"result\" : {\n    \"status\" : \"FAILURE\",\n    \"summary\" : \"Couldn't read the page\",\n    \"result\" : {\n      \"key\" : \"value\"\n    },\n    \"error\" : \"Could not read the page\"\n  }\n} ]", result);
    }

}