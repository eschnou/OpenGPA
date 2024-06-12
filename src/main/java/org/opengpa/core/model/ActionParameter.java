package org.opengpa.core.model;

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

    private String name;
    private String description;
}
