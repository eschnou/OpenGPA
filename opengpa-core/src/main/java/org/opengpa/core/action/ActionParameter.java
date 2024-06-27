package org.opengpa.core.action;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonPropertyOrder({ "name", "description"})
public class ActionParameter {

    public static ActionParameter from(String name, String description) {
        return new ActionParameter(name, description);
    }

    // A descriptive name used by the Agent to define this parameter
    private String name;

    // A description helping the agent figuring out what to provide for value
    private String description;
}
