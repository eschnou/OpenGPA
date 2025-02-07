package org.opengpa.server.dto;

import lombok.Builder;
import lombok.Getter;
import org.opengpa.core.workspace.Document;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class ActionStreamEventDTO extends AbstractStreamEventDTO {

    private String taskId;
    private String action;
    private String reasoning;
    private Object result;

    @Builder
    public ActionStreamEventDTO(String owner, String summary, String reasoning, ZonedDateTime timestamp, String taskId, String action, List<Document> documents, Object result) {
        super(owner, summary, documents, timestamp);
        this.taskId = taskId;
        this.action = action;
        this.result = result;
        this.reasoning = reasoning;
    }
}
