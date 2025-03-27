package org.opengpa.server.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TaskDTO {
    private ZonedDateTime created;
    private ZonedDateTime completed;
    private String id;
    private String title;
    private String description;
    private String request;
    private Map<String, String> context;
    private List<String> enabledCategories;
}
