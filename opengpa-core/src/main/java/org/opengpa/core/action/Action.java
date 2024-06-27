package org.opengpa.core.action;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.opengpa.core.agent.Agent;

import java.util.List;
import java.util.Map;

@JsonPropertyOrder({ "name", "description", "parameters"})
public interface Action {

    public String getName();

    public String getDescription();

    public List<ActionParameter> getParameters();

    public ActionResult apply(Agent agent, Map<String, String> input);

}
