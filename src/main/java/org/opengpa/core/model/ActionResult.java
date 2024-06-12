package org.opengpa.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({ "status", "summary", "result", "error"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResult {

    public enum Status {
        SUCCESS,
        FAILURE
    }

    // Could the engine perform the action or not, will be shared with the agent
    private Status status;

    // In case of error, some details so the agent can pick a new action
    private String error;

    // The result of the action, usually a complex object that will be serialized to JSON when passed back
    // to the model when moving to the next step.
    private Object result;

    // A user-friendly summary of the action, will be used to show to the user what the agent is doing
    private String summary;

    // If the action result in a user output message, it should be here
    @JsonIgnore
    private String output;

    // A list of documents created in the workspace by this action
    @Builder.Default
    @JsonIgnore
    private List<WorkspaceDocument> documents = new ArrayList<>();

}
