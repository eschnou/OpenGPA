package org.opengpa.core.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengpa.core.workspace.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({ "status", "state", "summary", "result", "error", "stateData"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResult {

    public enum Status {
        SUCCESS,
        FAILURE,
        AWAITING_INPUT,
        IN_PROGRESS
    }

    // Current status of the action execution
    private Status status;

    // Unique identifier for this action result, useful for tracking state
    @Builder.Default
    private String actionId = UUID.randomUUID().toString();

    // In case of error, some details so the agent can pick a new action
    private String error;

    // The result of the action, usually a complex object that will be serialized to JSON
    private Object result;

    // A user-friendly summary of the action, will be used to show to the user what the agent is doing
    private String summary;

    // Additional data specific to the current state, could include form fields, progress information, etc.
    private Map<String, String> stateData;

    // A list of documents created in the workspace by this action
    @Builder.Default
    @JsonIgnore
    private List<Document> documents = new ArrayList<>();

    // Helper methods for state management
    public boolean isCompleted() {
        return status == Status.SUCCESS || status == Status.FAILURE;
    }

    public boolean isAwaitingInput() {
        return status == Status.AWAITING_INPUT;
    }

    public boolean isInProgress() {
        return status == Status.IN_PROGRESS;
    }

    public boolean isFailed() {
        return status == Status.FAILURE;
    }

    // Static builders for common states
    public static ActionResult completed(Object result, String summary) {
        return ActionResult.builder()
                .status(Status.SUCCESS)
                .result(result)
                .summary(summary)
                .build();
    }

    public static ActionResult inProgress(String summary, Map<String, String> stateData) {
        return ActionResult.builder()
                .status(Status.IN_PROGRESS)
                .summary(summary)
                .stateData(stateData)
                .build();
    }

    public static ActionResult awaitingInput(String summary, Map<String, String> stateData) {
        return ActionResult.builder()
                .status(Status.AWAITING_INPUT)
                .summary(summary)
                .stateData(stateData)
                .build();
    }

    public static ActionResult failed(String error, String summary) {
        return ActionResult.builder()
                .status(Status.FAILURE)
                .error(error)
                .summary(summary)
                .build();
    }
}