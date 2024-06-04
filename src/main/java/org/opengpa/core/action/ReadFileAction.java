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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="readfile", havingValue = "true", matchIfMissing = true)
public class ReadFileAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(ReadFileAction.class);

    private final Workspace workspace;

    public ReadFileAction(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return "readFile";
    }

    @Override
    public String getDescription() {
        return "Read a file with the given filename.";
    }

    @Override
    public List<ActionParameter> getArguments() {
        return List.of(
                ActionParameter.from("filename", "Relative path of the file to read in the agent workspace")
        );
    }

    public ActionResult apply(Agent agent, Map<String, String> request) {
        log.debug("Reading file {} for agent {}", request.get("filename"), agent.getId());
        Optional<WorkspaceDocument> document = workspace.getDocumentByName(agent.getId(), request.get("filename"));
        if (document.isPresent()) {
            byte[] artifactContent = workspace.getDocumentContent(agent.getId(), document.get().getDocumentId());
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .output(new String(artifactContent))
                    .message(String.format("Successfully read file %s from the agent workspace.", document.get().getName()))
                    .build();
        } else {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .message(String.format("I could not find the file %s in the workspace.", request.get("filename")))
                    .error("File not found.")
                    .build();
        }
    }
}
