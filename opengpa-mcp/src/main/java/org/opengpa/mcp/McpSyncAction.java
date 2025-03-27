package org.opengpa.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AllArgsConstructor;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.JsonSchemaUtils;
import org.opengpa.core.agent.Agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class McpSyncAction implements Action {

    private final McpSchema.Tool tool;

    private final McpSyncClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    public McpSyncAction(McpSyncClient client, McpSchema.Tool tool) {
        this.tool = tool;
        this.client = client;
    }

    @Override
    public String getName() {
        return tool.name();
    }

    @Override
    public String getDescription() {
        return tool.description();
    }
    
    @Override
    public String getCategory() {
        return client.getServerInfo().name();
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            String schemaString = mapper.writeValueAsString(tool.inputSchema());
            return mapper.readTree(schemaString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, Object> input, Map<String, String> context) {
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(getName(), input);
        McpSchema.CallToolResult callToolResult = client.callTool(request);
        if (callToolResult == null) {
            return ActionResult.failed(
                    String.format("Calling %s action failed without errors.", tool.name()),
                    "Failed at executing the requested action."
            );
        }

        Boolean error = callToolResult.isError() == null ? false : callToolResult.isError();
        ActionResult actionResult = ActionResult.builder()
                .status(error  ? ActionResult.Status.FAILURE : ActionResult.Status.SUCCESS)
                .result(callToolResult.content())
                .actionId(UUID.randomUUID().toString())
                .summary(String.format("I've executed the %s tool from %s service", tool.name(), client.getServerInfo().name()))
                .build();

        return actionResult;
    }

    @Override
    public ActionResult continueAction(Agent agent, String actionId, Map<String, String> stateData, Map<String, String> context) {
        return Action.super.continueAction(agent, actionId, stateData, context);
    }

    @Override
    public ActionResult cancelAction(Agent agent, String actionId) {
        return Action.super.cancelAction(agent, actionId);
    }
}
