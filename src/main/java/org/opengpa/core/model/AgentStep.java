package org.opengpa.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AgentStep
{

    String input;

    Map<String, String> context;

    ActionInvocation action;

    String reasoning;

    String feedback;

    ActionResult result;

    boolean isFinal;
}
