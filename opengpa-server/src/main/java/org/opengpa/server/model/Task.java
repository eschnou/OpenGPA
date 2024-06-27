package org.opengpa.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Task {

  private String taskId;

  private String input;

  private Map<String, String> context;

}

