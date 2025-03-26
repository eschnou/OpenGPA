package org.opengpa.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class McpActionProvider {

    private final List<McpSyncClient> mcpSyncClients;

    public List<Action> getMCPActions() {
        List<Action> actions = new ArrayList<>();
        for (McpSyncClient mcpSyncClient : mcpSyncClients) {
            for (McpSchema.Tool tool : mcpSyncClient.listTools().tools()) {
                log.debug("MCP client {} - tool {}", mcpSyncClient.getServerInfo().name(), tool.name());
                actions.add(new McpSyncAction(mcpSyncClient, tool));
            }
        }
        return actions;
    }
}
