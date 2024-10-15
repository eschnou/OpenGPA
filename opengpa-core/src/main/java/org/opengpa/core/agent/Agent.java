package org.opengpa.core.agent;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Agent {

    String getTask();

    List<AgentStep> getSteps();

    Map<String, String> getContext();

    String getId();

    Date getStartTime();

    AgentStep executeNextStep(String userInput, Map<String, String> context);
}
