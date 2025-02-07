package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RagChunkDTO {
    private String id;
    private String documentId;
    private String documentTitle;
    private String documentDescription;
    private String content;
}
