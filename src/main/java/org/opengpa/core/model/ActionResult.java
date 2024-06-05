package org.opengpa.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionResult {

    public enum Status {
        SUCCESS,
        FAILURE
    }

    // Could the engine perform the action or not, will be shared with the agent
    private Status status;

    // In case of error, some details so the agent can pick a new action
    private String error;

    // The result of the action, usually a complex object serialized to JSON. Will be passed back
    // to the model when moving to the next step.
    private String result;

    // A user-friendly summary of the action, will be used to show to the user what the agent is doing
    private String summary;

    // If the action result in a user output message, it should be here
    private String output;

    // A list of documents created in the workspace by this action
    @Builder.Default
    private List<WorkspaceDocument> documents = new ArrayList<>();

}
