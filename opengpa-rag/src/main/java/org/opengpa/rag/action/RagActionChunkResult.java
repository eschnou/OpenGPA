package org.opengpa.rag.action;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RagActionChunkResult {
    private String id;
    private String documentId;
    private String documentTitle;
    private String documentDescription;
    private String content;
}
