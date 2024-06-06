package org.opengpa.server.service;

import org.opengpa.core.action.Action;
import org.opengpa.core.agent.ActionAgent;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.AgentStep;
import org.opengpa.server.config.ApplicationConfig;
import org.opengpa.server.dto.Artifact;
import org.opengpa.server.dto.Step;
import org.opengpa.server.dto.Task;
import org.opengpa.server.exceptions.TaskNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskService {

    private final HashMap<String, Agent> tasks = new HashMap<>();

    private final HashMap<String, List<Step>> steps = new HashMap<>();

    private final ChatClient chatClient;

    private final List<Action> actions;

    private final ApplicationConfig applicationConfig;

    @Autowired
    public TaskService(ChatClient chatClient, List<Action> actions, ApplicationConfig applicationConfig) {
        this.chatClient = chatClient;
        this.actions = actions;
        this.applicationConfig = applicationConfig;
    }

    public Task plan(String task, Map<String, String> additionalInputs) {
        ActionAgent agent = new ActionAgent(chatClient, actions, task, additionalInputs);
        if (applicationConfig.isLogPrompt()) {
            agent.enableLogging(applicationConfig.getLogFolder());
        }

        tasks.put(agent.getId(), agent);

        return Task.builder()
                .input(task)
                .additionalInput(additionalInputs)
                .taskId(agent.getId())
                .build();
    }

    @Async
    public ListenableFuture<Step> asyncNextStep(String task, String userInput, Map<String, String> additionalInputs) {
        return AsyncResult.forValue(nextStep(task, userInput, additionalInputs));
    }

    public Step nextStep(String taskId, String userInput, Map<String, String> context) {
        if (!tasks.containsKey(taskId)) {
            log.warn("Task with id {} not found", taskId);
            throw new TaskNotFoundException(taskId.toString());
        }

        Agent agent = tasks.get(taskId);
        AgentStep step = agent.executeNextStep(userInput, context);

        Step completedStep = Step.builder()
                .taskId(taskId.toString())
                .input(userInput)
                .additionalInput(context)
                .reasoning(step.getReasoning())
                .action(step.getResult().getSummary())
                .output(step.getResult().getOutput())
                .status(Step.StatusEnum.COMPLETED)
                .stepId(UUID.randomUUID().toString())
                .artifacts(step.getResult().getDocuments().stream().map(Artifact::fromWorkspaceDocument).toList())
                .isLast(step.isFinal())
                .build();

        List<Step> taskSteps = steps.getOrDefault(taskId, new ArrayList<>());
        taskSteps.add(completedStep);
        steps.put(taskId, taskSteps);

        return completedStep;
    }

    public List<Task> getTasks() {
        return tasks.values().stream()
                .sorted(Comparator.comparing(Agent::getStartTime))
                .map(agent -> getTask(agent.getId()))
                .collect(Collectors.toList());
    }

    public Task getTask(String taskId) {
        Agent agent = this.tasks.get(taskId);

        if (agent == null) {
            throw new TaskNotFoundException(taskId);
        }

        return Task.builder()
                .input(agent.getTask())
                .additionalInput(agent.getContext())
                .taskId(taskId)
                .build();
    }

    public List<Step> getSteps(String taskId) {
        Agent agent = this.tasks.get(taskId);

        if (agent == null) {
            throw new TaskNotFoundException(taskId);
        }

        if (!steps.containsKey(taskId)) {
            return new ArrayList<>();
        }

        return steps.get(taskId);
    }
}
