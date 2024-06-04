package org.opengpa.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskListResponse
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskListResponse {

  @Valid
  @Builder.Default
  private List<Task> tasks = new ArrayList<>();

  private Pagination pagination;

}

