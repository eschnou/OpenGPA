package org.opengpa.server.mapper;

import org.opengpa.server.dto.TaskDTO;
import org.opengpa.server.model.Task;

public class TaskMapper {

    public static TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getTaskId());
        dto.setContext(task.getContext());
        dto.setCreated(task.getCreated());
        dto.setCompleted(task.getCompleted());
        dto.setTitle(task.getTitle());
            dto.setRequest(task.getAgent().getTask());
        dto.setDescription(task.getDescription());
        return dto;
    }
}
