package org.opengpa.core.agent.react;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.browser.RawBrowserAction;
import org.opengpa.core.action.search.SearchWebAction;
import org.opengpa.core.agent.ActionInvocation;
import org.opengpa.core.agent.AgentStep;

import java.util.*;

class ReActAgentTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void renderTools() {
        List<Action> actions = Arrays.asList(
                new RawBrowserAction(null),
                new SearchWebAction()
        );

        ReActAgent reActAgent = new ReActAgent(null, null, null, "Task", new HashMap<>());
        String result = reActAgent.renderTools(actions);
        Assertions.assertEquals("[ {\n" +
                "  \"name\" : \"browse_web\",\n" +
                "  \"description\" : \"Fetch the raw html content at a given url.\",\n" +
                "  \"data\" : { },\n" +
                "  \"parameters\" : {\n" +
                "    \"type\" : \"object\",\n" +
                "    \"title\" : \"Raw Browser Parameters\",\n" +
                "    \"properties\" : {\n" +
                "      \"url\" : {\n" +
                "        \"type\" : \"string\",\n" +
                "        \"description\" : \"The url of the page to load.\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"required\" : [ \"url\" ]\n" +
                "  }\n" +
                "}, {\n" +
                "  \"name\" : \"search_web\",\n" +
                "  \"description\" : \"Search the web with the given query.\",\n" +
                "  \"data\" : { },\n" +
                "  \"parameters\" : {\n" +
                "    \"type\" : \"object\",\n" +
                "    \"title\" : \"Action Parameters\",\n" +
                "    \"properties\" : {\n" +
                "      \"query\" : {\n" +
                "        \"type\" : \"string\",\n" +
                "        \"description\" : \"The query to pass to the web search engine.\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"required\" : [ \"query\" ]\n" +
                "  }\n" +
                "} ]", result);
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
    void renderSteps() throws Exception {
        ReActAgent reActAgent = new ReActAgent(null, null, null, "Task", new HashMap<>());

        // Create steps with fixed UUIDs instead of random ones
        UUID step1Id = UUID.fromString("56812682-ecce-4418-8aa9-b639c11d9e5f");
        UUID step1ActionId = UUID.fromString("689e15bc-4b02-4463-ac77-bd345a45e7d4");
        UUID step2Id = UUID.fromString("b85d7b4d-14f8-4eaf-9a68-345c81fa7580");
        UUID step2ActionId = UUID.fromString("f10e7f90-494b-450b-b728-3caed5bf663e");

        List<AgentStep> steps = Arrays.asList(
                AgentStep.builder()
                        .id(step1Id.toString())
                        .input("Task 1")
                        .reasoning("Reason for doing this action for 1")
                        .isFinal(false)
                        .action(ActionInvocation.builder().name("web_search").parameters(Map.of("query", "what is the weather")).build())
                        .result(ActionResult.builder()
                                .actionId(step1ActionId.toString())
                                .status(ActionResult.Status.SUCCESS)
                                .summary("Query returned two links")
                                .result(Arrays.asList("link1", "link2"))
                                .build())
                        .build(),
                AgentStep.builder()
                        .id(step2Id.toString())
                        .input("Task 2")
                        .reasoning("Reason for doing this action for 2")
                        .isFinal(true)
                        .action(ActionInvocation.builder().name("scrape_url").parameters(Map.of("query", "http://url")).build())
                        .result(ActionResult.builder()
                                .actionId(step2ActionId.toString())
                                .status(ActionResult.Status.FAILURE)
                                .summary("Couldn't read the page")
                                .result(Map.of("key", "value"))
                                .error("Could not read the page")
                                .build())
                        .build()
        );

        String result = reActAgent.renderPreviousSteps(steps);

        // Parse both JSON strings to compare structure instead of exact string match
        JsonNode resultJson = objectMapper.readTree(result);
        JsonNode expectedJson = objectMapper.readTree("[ {\n" +
                "  \"id\" : \"56812682-ecce-4418-8aa9-b639c11d9e5f\",\n" +
                "  \"input\" : \"Task 1\",\n" +
                "  \"reasoning\" : \"Reason for doing this action for 1\",\n" +
                "  \"action\" : {\n" +
                "    \"name\" : \"web_search\",\n" +
                "    \"parameters\" : {\n" +
                "      \"query\" : \"what is the weather\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"final\" : false,\n" +
                "  \"result\" : {\n" +
                "    \"status\" : \"SUCCESS\",\n" +
                "    \"summary\" : \"Query returned two links\",\n" +
                "    \"result\" : [ \"link1\", \"link2\" ],\n" +
                "    \"actionId\" : \"689e15bc-4b02-4463-ac77-bd345a45e7d4\"\n" +
                "  }\n" +
                "}, {\n" +
                "  \"id\" : \"b85d7b4d-14f8-4eaf-9a68-345c81fa7580\",\n" +
                "  \"input\" : \"Task 2\",\n" +
                "  \"reasoning\" : \"Reason for doing this action for 2\",\n" +
                "  \"action\" : {\n" +
                "    \"name\" : \"scrape_url\",\n" +
                "    \"parameters\" : {\n" +
                "      \"query\" : \"http://url\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"final\" : true,\n" +
                "  \"result\" : {\n" +
                "    \"status\" : \"FAILURE\",\n" +
                "    \"summary\" : \"Couldn't read the page\",\n" +
                "    \"result\" : {\n" +
                "      \"key\" : \"value\"\n" +
                "    },\n" +
                "    \"error\" : \"Could not read the page\",\n" +
                "    \"actionId\" : \"f10e7f90-494b-450b-b728-3caed5bf663e\"\n" +
                "  }\n" +
                "} ]");

        Assertions.assertEquals(expectedJson, resultJson);
    }

}