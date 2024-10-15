package org.opengpa.core.agent.react;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import org.opengpa.core.action.ActionParameter;

import java.util.List;
import java.util.Map;

@JsonPropertyOrder({ "name", "description", "parameters", "data"})
@Data
@Builder
public class ActionDTO {
    private String name;
    private String description;
    private List<ActionParameter> parameters;
    private Map<String, Object> data;
}
