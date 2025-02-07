package org.opengpa.server.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ActionDTO {

    private String name;

    private Map<String, String> parameters;

    String reasoning;

    boolean isFinal;
}
