package org.opengpa.server.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DocumentDTO {

    String taskId;
    String filename;
    private Map<String, String> metadata = new HashMap<>();
}
