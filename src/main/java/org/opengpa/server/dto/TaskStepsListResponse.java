package org.opengpa.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskStepsListResponse
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskStepsListResponse {

  @Valid
  @Builder.Default
  private List<Step> steps = new ArrayList<>();

  private Pagination pagination;

}

