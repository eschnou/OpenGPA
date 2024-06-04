package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.ActionResult;
import org.opengpa.core.model.ActionParameter;

import java.util.List;
import java.util.Map;

public interface Action {

    public String getName();

    public String getDescription();

    public List<ActionParameter> getArguments();

    public ActionResult apply(Agent agent, Map<String, String> input);

}
