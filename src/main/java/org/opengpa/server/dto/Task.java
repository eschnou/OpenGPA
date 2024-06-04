package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Task
 */

@Data
@Builder
public class Task {

  private String input;

  @JsonProperty("additional_input")
  private Map<String, String> additionalInput;

  @JsonProperty("task_id")
  private String taskId;

  @Valid
  @Builder.Default
  private List<Artifact> artifacts = new ArrayList<>();

}

