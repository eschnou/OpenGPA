package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StepDTO {

    String input;

    ActionDTO action;

    ResultDTO result;

    List<DocumentDTO> documents;
}