package org.opengpa.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AllArgsConstructor;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class McpSyncAction implements Action {

    private final McpSchema.Tool tool;

    private final McpSyncClient client;

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
    public List<ActionParameter> getParameters() {
        tool.inputSchema().
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, String> input, Map<String, String> context) {
        Map<String, Object> parameters = new HashMap<>();
        input.keySet().stream().forEach(key -> {parameters.put(key, input.get(key));});

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(getName(), parameters);
        McpSchema.CallToolResult callToolResult = client.callTool(request);

        ActionResult actionResult = ActionResult.builder()
                .status(callToolResult.isError() ? ActionResult.Status.FAILURE : ActionResult.Status.SUCCESS)
                .result(callToolResult.content())
                .actionId(UUID.randomUUID().toString())
                .summary(String.format("I've executed the %s tool from %s service", tool.name(), client.getClientInfo().name()))
                .build();

        return actionResult;
    }
}
