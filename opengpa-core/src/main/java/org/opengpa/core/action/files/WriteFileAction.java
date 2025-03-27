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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions.files", name="enabled", havingValue = "true", matchIfMissing = false)
public class WriteFileAction extends LegacyActionAdapter {

    private static final Logger log = LoggerFactory.getLogger(WriteFileAction.class);

    private final Workspace workspace;

    public WriteFileAction(Workspace workspace) {
        log.info("Creating WriteFileAction");
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
    public String getCategory() {
        return "core";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("filename", "Relative path of the file to write in the agent workspace"),
                ActionParameter.from("body", "Text content to write to the file. Binary is not supported.")
        );
    }

    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> request,  Map<String, String> context) {
        log.debug("Writing content to file {}", request.get("filename"));

        String filename = request.get("filename");
        if (filename == null || filename.isEmpty()) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("The filename parameter is missing or has an empty value.")
                    .error("The filename parameter is missing or has an empty value.")
                    .build();
        }

        Document document = workspace.addDocument(agent.getId(), filename, request.get("body").getBytes(), new HashMap<>());

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .summary(String.format("The file %s has been written to the Agent workspace.", request.get("filename")))
                .result(String.format("The file %s has been written to the Agent workspace.", request.get("filename")))
                .documents(Arrays.asList(document))
                .build();
    }
}
