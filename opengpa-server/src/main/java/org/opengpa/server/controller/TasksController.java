package org.opengpa.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.opengpa.core.agent.AgentStep;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.server.dto.InputDTO;
import org.opengpa.server.dto.StepDTO;
import org.opengpa.server.dto.TaskDTO;
import org.opengpa.server.exceptions.DocumentNotFoundException;
import org.opengpa.server.mapper.StepMapper;
import org.opengpa.server.mapper.TaskMapper;
import org.opengpa.server.model.Task;
import org.opengpa.server.service.TaskService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "Endpoint for creating, progressing and managing Agent tasks.")
public class TasksController {

    private final TaskService taskService;

    private final Workspace workspace;

    public TasksController(TaskService taskService, Workspace workspace) {
        this.taskService = taskService;
        this.workspace = workspace;
    }

    @GetMapping
    public List<TaskDTO> listTasks(Principal principal) {
        log.debug("listTasks username={}", principal.getName());
        List<Task> tasks = taskService.getTasks(principal.getName());
        return tasks.stream().map(TaskMapper::toDTO).toList();
    }

    @PostMapping()
    public ResponseEntity<TaskDTO> newTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InputDTO inputDTO
    ) {
        log.debug("newTask username={} message={}", userDetails.getUsername(), inputDTO.getMessage());
        Task task = taskService.plan(userDetails.getUsername(), inputDTO.getMessage(), new HashMap<>());
        return ResponseEntity.ok(TaskMapper.toDTO(task));
    }

    @GetMapping(value = "/{task_id}")
    public TaskDTO getTask(Principal principal, @PathVariable("task_id") String taskId) {
        log.debug("getTasl username={} taskId={}", principal.getName(), taskId);

        Task task = taskService.getTask(principal.getName(), taskId);
        return TaskMapper.toDTO(task);
    }

    @PostMapping(value = "/{task_id}")
    public StepDTO progressTask(Principal principal, @PathVariable("task_id") String taskId, @Valid @RequestBody InputDTO inputDTO) {
        log.debug("progressTask username={} taskId={}", principal.getName(), taskId);
        AgentStep agentStep = taskService.nextStep(principal.getName(), taskId, inputDTO.getMessage(), inputDTO.getStateData(), new HashMap<>());
        return StepMapper.toDTO(agentStep);
    }

    @GetMapping(value = "/{task_id}/steps")
    public List<StepDTO> listSteps(Principal principal, @PathVariable("task_id") String taskId) {
        log.debug("listSteps username={} taskId={}", principal.getName(), taskId);

        List<AgentStep> steps = taskService.getSteps(principal.getName(), taskId);
        return steps.stream().map(StepMapper::toDTO).toList();
    }

    @PostMapping(value = "/{task_id}/documents")
    public ResponseEntity<Document> uploadDocumentToWorkspace(Principal principal,
                                                              @PathVariable("task_id") String taskId,
                                                              @RequestPart("file") MultipartFile file) {
        try {
            // Check that the user owns the task
            Task task = taskService.getTask(principal.getName(), taskId);

            // Convert the MultipartFile to a byte array
            InputStream inputStream = file.getInputStream();
            byte[] fileContent = new byte[(int) file.getSize()];
            IOUtils.readFully(inputStream, fileContent);

            // Add the document to the workspace
            Document document = workspace.addDocument(taskId, file.getOriginalFilename(), fileContent,
                    Collections.singletonMap("uploadedBy", principal.getName()));

            // Return the document metadata along with a CREATED status code
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    @GetMapping(value = "/{task_id}/documents/{document_id}", produces = "application/octet-stream")
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
