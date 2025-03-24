package org.opengpa.core.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import org.opengpa.core.action.ActionResult;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@JsonPropertyOrder({ "id", "input", "reasoning", "action", "final", "result", "feedback", "state"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AgentStep {

    @Builder.Default
    String id = UUID.randomUUID().toString();

    String input;

    @JsonIgnore
    Map<String, String> context;

    ActionInvocation action;

    String reasoning;

    String feedback;

    ActionResult result;

    boolean isFinal;

    // Helper methods for state checking
    public boolean isCompleted() {
        return result != null && result.isCompleted();
    }

    public boolean isAwaitingInput() {
        return result != null && result.isAwaitingInput();
    }

    public boolean isInProgress() {
        return result != null && result.isInProgress();
    }

    public boolean needsContinuation() {
        return result != null && !result.isCompleted() && !result.isFailed();
    }
}