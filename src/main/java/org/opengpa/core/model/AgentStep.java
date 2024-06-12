package org.opengpa.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonPropertyOrder({ "input", "reasoning", "action", "final", "result", "feedback"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AgentStep
{

    String input;

    @JsonIgnore
    Map<String, String> context;

    ActionInvocation action;

    String reasoning;

    String feedback;

    ActionResult result;

    boolean isFinal;
}
