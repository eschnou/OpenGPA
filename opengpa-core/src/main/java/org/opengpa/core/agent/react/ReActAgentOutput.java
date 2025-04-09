package org.opengpa.core.agent.react;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengpa.core.agent.ActionInvocation;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({ "reasoning", "action"})
public class ReActAgentOutput {

    @JsonProperty(required = true)
    private String reasoning;

    @JsonProperty(required = true)
    private ActionInvocation action;

}
