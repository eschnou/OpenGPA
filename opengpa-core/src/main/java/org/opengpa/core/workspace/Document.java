package org.opengpa.core.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {

  private String workspaceId;

  private String name;

  @Builder.Default
  private Map<String, String> metadata = new HashMap<>();

}

