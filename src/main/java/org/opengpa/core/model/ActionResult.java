package org.opengpa.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionResult {

    public enum Status {
        SUCCESS,
        FAILURE
    }

    // The output of the action, usually a complex object serialized to JSON
    private String output;

    // A user-friendly message summarizing the action outcome
    private String message;

    private Status status;

    private String error;

    // A list of documents created in the workspace by this action
    @Builder.Default
    private List<WorkspaceDocument> documents = new ArrayList<>();

}
