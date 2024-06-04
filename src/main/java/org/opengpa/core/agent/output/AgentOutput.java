package org.opengpa.core.agent.output;

import org.opengpa.core.model.ActionInvocation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentOutput {

    private ActionInvocation action;

    @JsonProperty("is_final")
    private boolean isFinal;

    private String reasoning;

}
