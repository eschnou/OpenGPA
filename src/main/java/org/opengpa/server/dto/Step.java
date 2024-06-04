package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Step
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Step {

  private String input;

  @Builder.Default
  @JsonProperty("additional_input")
  private Map<String, String> additionalInput = new HashMap<>();

  @JsonProperty("task_id")
  private String taskId;

  @JsonProperty("step_id")
  private String stepId;

  private String name;

  private StatusEnum status;

  private String output;

  @Builder.Default
  @JsonProperty("additional_output")
  private Map<String, String> additionalOutput = new HashMap<>();

  @Valid
  @Builder.Default
  private List<Artifact> artifacts = new ArrayList<>();

  @JsonProperty("is_last")
  @Builder.Default
  private Boolean isLast = false;

  /**
   * The status of the task step.
   */
  public enum StatusEnum {
    CREATED("created"),
    
    RUNNING("running"),
    
    COMPLETED("completed");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }
}

