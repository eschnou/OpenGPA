package org.opengpa.server.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.server.exceptions.DocumentNotFoundException;
import org.opengpa.server.model.Task;
import org.opengpa.server.service.TaskService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.security.Principal;
import java.util.Optional;

@Slf4j
@PermitAll
@RestController
@RequestMapping("/api/files")
public class FilesController {

    private final TaskService taskService;
    private final Workspace workspace;

    public FilesController(TaskService taskService, Workspace workspace) {
        this.taskService = taskService;
        this.workspace = workspace;
    }

    @GetMapping(value = "/{task_id}/documents/{document_id}", produces ="application/octet-stream")
    public ResponseEntity<Resource> downloadAgentTaskArtifact(Principal principal, @PathVariable("task_id") String taskId, @PathVariable("document_id") String artifactId) {
        log.debug("downloadAgentTaskArtifact username={} taskId={} artifactId={}", principal.getName(), taskId, artifactId);

        // First check the user own the task, will emit TaskNotFound of the task doesn't belong to the user
        Task task = taskService.getTask(principal.getName(), taskId);

        Optional<Document> document = workspace.getDocument(taskId, artifactId);
        if (document.isEmpty()) {
            throw new DocumentNotFoundException(artifactId);
        }

        byte[] artifactContent = workspace.getDocumentContent(taskId, artifactId);
        Resource resource = new ByteArrayResource(artifactContent);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.get().getName() + "\"")
                .body(resource);
    }
}
