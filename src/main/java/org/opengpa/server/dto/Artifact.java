package org.opengpa.server.dto;

import org.opengpa.core.model.WorkspaceDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An Artifact either created by or submitted to the agent.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Artifact {

  @JsonProperty("artifact_id")
  private String artifactId;

  @JsonProperty("agent_created")
  private Boolean agentCreated;

  @JsonProperty("file_name")
  private String fileName;

  @JsonProperty("relative_path")
  private String relativePath;

  public static Artifact fromWorkspaceDocument(WorkspaceDocument workspaceDocument) {
    return Artifact.builder()
            .artifactId(workspaceDocument.getDocumentId())
            .relativePath(workspaceDocument.getRelativePath())
            .fileName(workspaceDocument.getName())
            .agentCreated(!Boolean.valueOf(workspaceDocument.getMetadata().getOrDefault("userContent", "false")).booleanValue())
            .build();
  }
}

