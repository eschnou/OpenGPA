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

    ActionResult apply(Agent agent, Map<String, String> input, Map<String, String> context);

}
