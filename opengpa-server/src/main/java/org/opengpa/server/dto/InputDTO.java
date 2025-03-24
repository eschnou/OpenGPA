package org.opengpa.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class InputDTO {

    String message;

    Map<String, String> stateData;
}
