package org.opengpa.server.model;

import lombok.Builder;
import lombok.Data;
import org.opengpa.core.agent.Agent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Task {

  public enum Status {
    SUCCESS,
    FAILURE
  }

  private ZonedDateTime created;

  private ZonedDateTime completed;

  private String taskId;

  private String title;

  private String description;

  private Map<String, String> context;

  private Status status;

  private Agent agent;

}

