package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultDTO {

    private String status;

    private Object details;

    private String summary;

    private String error;

    private String message;
}
