package org.opengpa.core.action.files;

import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.LegacyActionAdapter;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix="opengpa.actions.files", name="enabled", havingValue = "true", matchIfMissing = false)
public class ReadFileAction extends LegacyActionAdapter {

    private static final Logger log = LoggerFactory.getLogger(ReadFileAction.class);

    private final Workspace workspace;

    public ReadFileAction(Workspace workspace) {
        log.info("Creating ReadFileAction");
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
    public String getCategory() {
        return "core";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("filename", "Relative path of the file to read in the agent workspace")
        );
    }

    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> request,  Map<String, String> context) {
        log.debug("Reading file {} for agent {}", request.get("filename"), agent.getId());
        Optional<Document> document = workspace.getDocument(agent.getId(), request.get("filename"));
        if (document.isPresent()) {
            byte[] artifactContent = workspace.getDocumentContent(agent.getId(), request.get("filename"));
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .result(new String(artifactContent))
                    .summary(String.format("Successfully read file %s from the agent workspace.", document.get().getName()))
                    .build();
        } else {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary(String.format("I could not find the file %s in the workspace.", request.get("filename")))
                    .error("File not found.")
                    .build();
        }
    }
}
