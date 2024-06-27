package org.opengpa.core.agent.react;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengpa.core.agent.ActionInvocation;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReActAgentOutput {

    private ActionInvocation action;

    @JsonProperty("is_final")
    private boolean isFinal;

    private String reasoning;

}
