package org.opengpa.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Body of the task request.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StepRequestBody {

  @Builder.Default
  private String input = "";

  @Builder.Default
  private Map<String, String> additionalInput = new HashMap<>();
}

