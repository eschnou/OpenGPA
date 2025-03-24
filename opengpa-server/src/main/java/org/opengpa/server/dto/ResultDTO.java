package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.opengpa.core.action.ActionResult;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultDTO {

    private String status;

    private Object details;

    private String summary;

    private String error;

    private String message;

    private String actionId;

    private Map<String, String> stateData;
}