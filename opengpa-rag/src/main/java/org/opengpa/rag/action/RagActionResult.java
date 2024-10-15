package org.opengpa.rag.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class RagActionResult {

    private String content;

    private List<RagActionChunkResult> chunks;

}
