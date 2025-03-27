package org.opengpa.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InputDTO {

    String message;

    Map<String, String> stateData;
    
    List<String> enabledCategories;
}
