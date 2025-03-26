package org.opengpa.server.config;

import lombok.AllArgsConstructor;
import org.opengpa.core.action.Action;
import org.opengpa.mcp.McpActionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AllArgsConstructor
public class McpConfig {

    private final McpActionProvider mcpActionProvider;

    @Bean
    public List<Action> mcpActions() {
        return mcpActionProvider.getMCPActions();
    }
}
