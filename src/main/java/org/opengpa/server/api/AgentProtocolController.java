package org.opengpa.server.api;

import org.opengpa.server.exceptions.ArtifactNotFoundException;
import org.opengpa.server.service.TaskService;
import org.opengpa.server.service.ArtifactService;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.server.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequestMapping("${openapi.agentProtocol.base-path:}")
public class AgentProtocolController implements AgentProtocolApi {

    private final TaskService taskService;

    private final ArtifactService artifactService;

    @Autowired
    public AgentProtocolController(TaskService taskService, ArtifactService artifactService) {
        this.taskService = taskService;
        this.artifactService = artifactService;
    }

    @Override
    public ResponseEntity<Task> createAgentTask(Optional<TaskRequestBody> taskRequest) {
        log.debug("createAgentTask request={}", taskRequest);
        TaskRequestBody taskRequestBody = taskRequest.orElseGet(TaskRequestBody::new);
        Task task = taskService.plan(taskRequestBody.getInput(), taskRequestBody.getAdditionalInput());
        return ResponseEntity.ok(task);
    }

    @Override
    public ResponseEntity<Step> executeAgentTaskStep(String taskId, Optional<StepRequestBody> stepRequest) {
        log.debug("executeAgentTaskStep request={}", stepRequest);
        StepRequestBody stepRequestBody = stepRequest.orElseGet(StepRequestBody::new);
        Step step = taskService.nextStep(taskId, stepRequestBody.getInput(), stepRequestBody.getAdditionalInput());
        return ResponseEntity.ok(step);
    }

    @Override
    public ResponseEntity<Task> getAgentTask(String taskId) {
        log.debug("getAgentTask taskId={}", taskId);
        Task task = taskService.getTask(taskId);
        return ResponseEntity.ok(task);
    }

    @Override
    public ResponseEntity<Step> getAgentTaskStep(String taskId, String stepId) {
        log.debug("getAgentTaskStep taskId={} stepId={}", taskId, stepId);
        List<Step> steps = taskService.getSteps(taskId);
        Optional<Step> step = steps.stream()
                .filter(s -> s.getStepId().equals(stepId))
                .findFirst();
        if (step.isPresent()) {
            return ResponseEntity.ok(step.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<TaskStepsListResponse> listAgentTaskSteps(String taskId, Integer currentPage, Integer pageSize) {
        log.debug("listAgentTaskSteps taskId={}", taskId);

        if (currentPage == null || currentPage < 1) {
            throw new IllegalArgumentException("currentPage must be a positive integer");
        }

        List<Step> steps = taskService.getSteps(taskId);
        int totalItems = steps.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int fromIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Step> searchedSteps = steps.subList(fromIndex, endIndex);
        Pagination pagination = Pagination.builder().currentPage(currentPage).pageSize(pageSize).totalPages(totalPages).totalItems(totalItems).build();
        TaskStepsListResponse response = TaskStepsListResponse.builder()
                .steps(searchedSteps)
                .pagination(pagination)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TaskListResponse> listAgentTasks(Integer currentPage, Integer pageSize) {
        log.debug("listAgentTasks");

        if (currentPage == null || currentPage < 1) {
            throw new IllegalArgumentException("currentPage must be a positive integer");
        }

        List<Task> tasks = taskService.getTasks();
        int totalItems = tasks.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int fromIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Task> searchedTasks = tasks.subList(fromIndex, endIndex);
        TaskListResponse response = TaskListResponse.builder()
                .tasks(searchedTasks)
                .pagination(Pagination.builder().currentPage(currentPage).pageSize(pageSize).totalPages(totalPages).totalItems(totalItems).build())
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TaskArtifactsListResponse> listAgentTaskArtifacts(String taskId, Integer currentPage, Integer pageSize) {
        log.debug("listAgentTaskArtifacts taskId={}", taskId);

        List<Artifact> artifacts = artifactService.getFiles(taskId);
        int totalItems = artifacts.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        List<Artifact> paginatedArtifacts = artifacts.subList(startIndex, endIndex);

        Pagination pagination = Pagination.builder().currentPage(currentPage).pageSize(pageSize).totalPages(totalPages).totalItems(totalItems).build();
        TaskArtifactsListResponse response = TaskArtifactsListResponse.builder()
                .artifacts(paginatedArtifacts)
                .pagination(pagination)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Resource> downloadAgentTaskArtifact(String taskId, String artifactId) {
        log.debug("downloadAgentTaskArtifact taskId={} artifactId={}", taskId, artifactId);

        Optional<Artifact> artifact = artifactService.getArtifactById(taskId, artifactId);
        if (artifact.isEmpty()) {
            throw new ArtifactNotFoundException(artifactId);
        }

        byte[] artifactContent = artifactService.getArtifactContent(taskId, artifactId);

        Resource resource = new ByteArrayResource(artifactContent);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + artifact.get().getFileName() + "\"")
                .body(resource);
    }

    @Override
    public ResponseEntity<Artifact> uploadAgentTaskArtifacts(String taskId, MultipartFile file, String relativePath) {
        log.debug("uploadAgentTaskArtifacts taskId={} file={}", taskId, file.getOriginalFilename());

        try {
            Artifact artifact = artifactService.addArtifact(taskId, relativePath, file.getOriginalFilename(), file.getBytes(), false);
            return ResponseEntity.ok(artifact);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
