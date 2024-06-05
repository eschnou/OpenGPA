package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.ActionParameter;
import org.opengpa.core.model.ActionResult;
import org.opengpa.core.workspace.Workspace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="listfile", havingValue = "true", matchIfMissing = true)
public class ListFileAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(ListFileAction.class);

    private final Workspace workspace;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ListFileAction(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return "listFiles";
    }

    @Override
    public String getDescription() {
        return "List all files available in the task workspace.";
    }

    @Override
    public List<ActionParameter> getArguments() {
        return List.of(
        );
    }

    public ActionResult apply(Agent agent, Map<String, String> request) {
        log.debug("Listing files");

        List<String> list = workspace.getDocuments(agent.getId()).stream()
                .map(artifact -> artifact.getName())
                .distinct()
                .toList();

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .result(formatResult(list))
                .summary(String.format("Listing %d files in the workspace.", list.size()))
                .build();
    }

    private String formatResult(List<String> list) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
