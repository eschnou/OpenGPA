package org.opengpa.server.exceptions;

public class TaskNotFoundException extends RuntimeException {

    private final String taskId;

    public TaskNotFoundException(String taskId) {
        this.taskId = taskId;
    }
}
