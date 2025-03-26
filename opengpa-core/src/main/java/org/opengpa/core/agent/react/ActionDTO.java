package org.opengpa.core.agent.react;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@JsonPropertyOrder({ "name", "description", "schema", "data"})
@Data
@Builder
public class ActionDTO {
    private String name;
    private String description;
    
    // JSON Schema defining the input parameters structure and validation rules
    private JsonNode parameters;
    
    // Additional data that may be needed by the action
    private Map<String, Object> data;
}
