package org.opengpa.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * An Artifact either created by or submitted to the agent.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkspaceDocument {

  private String documentId;

  private String name;

  private String relativePath;

  @Builder.Default
  private Map<String, String> metadata = new HashMap<>();

}

