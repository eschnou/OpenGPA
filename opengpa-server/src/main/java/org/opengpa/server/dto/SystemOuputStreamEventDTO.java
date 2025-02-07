package org.opengpa.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Arrays;


@Getter
public class SystemOuputStreamEventDTO extends AbstractStreamEventDTO {

    private String taskId;

    @Builder
    public SystemOuputStreamEventDTO(String owner, String summary, ZonedDateTime timestamp, String taskId) {
        super(owner, summary, Arrays.asList(), timestamp);
        this.taskId = taskId;
    }
}
