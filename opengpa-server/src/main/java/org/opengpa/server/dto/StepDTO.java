package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StepDTO {

    String input;

    ActionDTO action;

    ResultDTO result;

    List<DocumentDTO> documents;

}
