package org.opengpa.core.action;

import com.fasterxml.jackson.databind.JsonNode;
import org.opengpa.core.agent.Agent;

import java.util.Collections;
import java.util.Map;

public interface Action {

    String getName();

    String getDescription();

    JsonNode getJsonSchema();

    ActionResult apply(Agent agent, Map<String, Object> input, Map<String, String> context);

    default Map<String, Object> getData(Map<String, String> context) {
        return Collections.emptyMap();
    }

    default ActionResult continueAction(Agent agent, String actionId,
                                        Map<String, String> stateData, Map<String, String> context) {
        // Default implementation just treats continuation as a new action
        throw new UnsupportedOperationException("This action doesn't support continueAction.");
    }

    default ActionResult cancelAction(Agent agent, String actionId) {
        // Default implementation just treats continuation as a new action
        throw new UnsupportedOperationException("This action doesn't support statefulExecution.");
    }
}