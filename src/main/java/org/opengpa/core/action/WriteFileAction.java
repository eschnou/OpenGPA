package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.ActionParameter;
import org.opengpa.core.model.ActionResult;
import org.opengpa.core.model.WorkspaceDocument;
import org.opengpa.core.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="files", havingValue = "true", matchIfMissing = false)
public class WriteFileAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(WriteFileAction.class);

    private final Workspace workspace;

    public WriteFileAction(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return "writeFile";
    }

    @Override
    public String getDescription() {
        return "Write a file with the given filename and body.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("filename", "Relative path of the file to write in the agent workspace"),
                ActionParameter.from("body", "Text content to write to the file. Binary is not supported.")
        );
    }

    public ActionResult apply(Agent agent, Map<String, String> request) {
        log.debug("Writing content to file {}", request.get("filename"));

        WorkspaceDocument workspaceDocument = workspace.addDocument(agent.getId(), "output/", request.get("filename"), request.get("body").getBytes(), new HashMap<>());

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .summary(String.format("The file %s has been written to the Agent workspace.", request.get("filename")))
                .result(String.format("The file %s has been written to the Agent workspace.", request.get("filename")))
                .documents(Arrays.asList(workspaceDocument))
                .build();
    }
}
