package org.opengpa.server.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ActionDTO {

    private String name;

    private Map<String, Object> parameters;

    String reasoning;

    boolean isFinal;
}
