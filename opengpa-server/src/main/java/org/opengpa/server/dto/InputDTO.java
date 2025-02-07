package org.opengpa.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InputDTO {

    @NotNull
    String message;
}
