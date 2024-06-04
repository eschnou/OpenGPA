package org.opengpa.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskArtifactsListResponse
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskArtifactsListResponse {

  @Valid
  @Builder.Default
  private List<Artifact> artifacts = new ArrayList<>();

  private Pagination pagination;
}

