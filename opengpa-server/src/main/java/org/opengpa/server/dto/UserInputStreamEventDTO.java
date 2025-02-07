package org.opengpa.server.dto;

import lombok.Builder;
import lombok.Getter;
import org.opengpa.core.workspace.Document;

import java.time.ZonedDateTime;
import java.util.List;


@Getter
public class UserInputStreamEventDTO extends AbstractStreamEventDTO {

    String taskId;

    @Builder
    public UserInputStreamEventDTO(String owner, String summary, ZonedDateTime timestamp, String taskId, List<Document> documents) {
        super(owner, summary, documents, timestamp);
        this.taskId = taskId;
    }
}
