package org.opengpa.core.action;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.opengpa.core.agent.Agent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Action {

    String getName();

    String getDescription();

    default List<ActionParameter> getParameters() {
        return Collections.emptyList();
    }

    default Map<String, Object> getData(Map<String, String> context) {
        return Collections.emptyMap();
    }

    /**
     * Initialize or execute the action
     */
    ActionResult apply(Agent agent, Map<String, String> input, Map<String, String> context);

    /**
     * Continue the execution of an action that was previously started and is in a non-completed state
     *
     * @param agent The agent that is executing the action
     * @param actionId The ID of the action that is being continued
     * @param stateData The current state data of the action
     * @param context Additional context for the action
     * @return The updated action result
     */
    default ActionResult continueAction(Agent agent, String actionId,
                                        Map<String, String> stateData, Map<String, String> context) {
        // Default implementation just treats continuation as a new action
        throw new UnsupportedOperationException("This action doesn't support continueAction.");
    }

    /**
     * Check if action supports stateful execution
     */
    default boolean supportsStatefulExecution() {
        return false;
    }

    /**
     * Cancel an in-progress action
     */
    default ActionResult cancelAction(Agent agent, String actionId) {
        return ActionResult.builder()
                .status(ActionResult.Status.FAILURE)
                .summary("Action was cancelled")
                .error("Action was cancelled by the user")
                .build();
    }
}