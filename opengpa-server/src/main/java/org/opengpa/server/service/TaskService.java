package org.opengpa.server.service;

import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.agent.AgentStep;
import org.opengpa.core.agent.react.ReActAgent;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.server.config.ApplicationConfig;
import org.opengpa.server.exceptions.TaskNotFoundException;
import org.opengpa.server.helper.topic.TopicService;
import org.opengpa.server.model.Task;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskService {

    // lists to hold agents and their respective steps
    private final HashMap<String, List<Task>> tasks = new HashMap<>();
    private final HashMap<String, List<AgentStep>> steps = new HashMap<>();

    // dependencies for the service
    private final ChatModel chatModel;
    private final Workspace workspace;
    private final TopicService topicService;
    private final List<Action> actions;
    private final ApplicationConfig applicationConfig;

    @Autowired
    public TaskService(ChatModel chatModel, Workspace workspace, TopicService topicService, List<Action> actions, ApplicationConfig applicationConfig) {
        this.chatModel = chatModel;
        this.workspace = workspace;
        this.topicService = topicService;
        this.actions = actions;
        this.applicationConfig = applicationConfig;
    }

    public Task plan(String username, String input, Map<String, String> additionalInputs) {
        List<Task> userTasks = tasks.getOrDefault(username, new ArrayList<>());
        additionalInputs.put("username", username);

        ReActAgent agent = new ReActAgent(chatModel, workspace, actions, input, additionalInputs);
        if (applicationConfig.isLogPrompt()) {
            agent.enableLogging(applicationConfig.getLogFolder());
        }

        Task task = Task.builder()
                .created(ZonedDateTime.now())
                .context(additionalInputs)
                .taskId(agent.getId())
                .agent(agent)
                .title(input.substring(0, Math.min(25, input.length())))
                .build();

        userTasks.add(task);
        tasks.put(username, userTasks);

        return task;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ListenableFuture<AgentStep> asyncNextStep(String username, String task, String userInput, Map<String, String> additionalInputs) {
        return AsyncResult.forValue(nextStep(username, task, userInput, additionalInputs));
    }

    @Transactional
    public AgentStep nextStep(String username, String taskId, String userInput, Map<String, String> context) {
        List<Task> userTasks = tasks.get(username);
        Optional<Task> task = userTasks.stream().filter(a -> a.getTaskId().equals(taskId)).findFirst();

        if (!task.isPresent()) {
            log.warn("Task with id {} not found", taskId);
            throw new TaskNotFoundException(taskId.toString());
        }

        AgentStep step = task.get().getAgent().executeNextStep(userInput, context);

        List<AgentStep> taskSteps = steps.getOrDefault(taskId, new ArrayList<>());
        taskSteps.add(step);
        steps.put(taskId, taskSteps);

        topicService.summarize(task.get()).ifPresent(s -> {
            task.get().setTitle(s.getTitle());
            task.get().setDescription(s.getSummary());
        });

        return step;
    }

    public List<Task> getTasks(String username) {
        List<Task> userTasks = tasks.getOrDefault(username, new ArrayList<>());
        return userTasks.stream()
                .sorted(Comparator.comparing(task -> task.getAgent().getStartTime()))
                .collect(Collectors.toList());
    }

    public List<AgentStep> getSteps(String username, String agentId) {
        Agent agent = getAgent(username, agentId);

        if (!steps.containsKey(agent.getId())) {
            return new ArrayList<>();
        }

        return steps.get(agentId);
    }

    public Task getTask(String username, String taskId) {
        List<Task> userTasks = tasks.getOrDefault(username, new ArrayList<>());
        Optional<Task> task = userTasks.stream().filter(a -> a.getTaskId().equals(taskId)).findFirst();

        if (!task.isPresent()) {
            throw new TaskNotFoundException(taskId);
        }

        return task.get();
    }

    private Agent getAgent(String username, String agentId) {
        List<Task> userTasks = tasks.getOrDefault(username, new ArrayList<>());
        Optional<Agent> agent = userTasks.stream().filter(a -> a.getTaskId().equals(agentId)).findFirst().map(Task::getAgent);

        if (!agent.isPresent()) {
            throw new TaskNotFoundException(agentId);
        }

        return agent.get();
    }
}
