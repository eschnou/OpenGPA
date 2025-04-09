package org.opengpa.core.action;

import lombok.Data;

@Data
public class CompleteTaskActionInput {


    public enum Status {
        SUCCESS, FAILED, ERROR
    }

    private String result;
    private Status status;

}
