package org.opengpa.core.action;

import com.fasterxml.jackson.databind.JsonNode;
import org.opengpa.core.agent.Agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter class that helps transition from the old Action interface (with String parameters)
 * to the new Action interface (with Object parameters and JSON Schema).
 * 
 * This class is intended to make migration easier for existing actions.
 */
public abstract class LegacyActionAdapter implements Action {

    @Override
    public JsonNode getJsonSchema() {
        return JsonSchemaUtils.generateSchemaFromParameters(getParameters());
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, Object> input, Map<String, String> context) {
        // Convert input from Map<String, Object> to Map<String, String> for backward compatibility
        Map<String, String> stringInput = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            // Convert objects to string representation
            if (entry.getValue() != null) {
                stringInput.put(entry.getKey(), entry.getValue().toString());
            } else {
                stringInput.put(entry.getKey(), null);
            }
        }

        // Call the original String-based implementation
        return applyStringParams(agent, stringInput, context);
    }

    /**
     * Define the parameters for this action
     * @return List of parameters
     */
    public abstract List<ActionParameter> getParameters();
    
    /**
     * Legacy implementation with string parameters
     * @param agent The agent executing the action
     * @param input Input parameters as strings
     * @param context Context information
     * @return Action result
     */
    public abstract ActionResult applyStringParams(Agent agent, Map<String, String> input, Map<String, String> context);
}