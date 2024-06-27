package org.opengpa.server.service;

import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.agent.AgentStep;
import org.opengpa.core.agent.react.ReActAgent;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.server.config.ApplicationConfig;
import org.opengpa.server.exceptions.TaskNotFoundException;
import org.opengpa.server.model.Task;
import org.springframework.ai.chat.model.ChatModel;
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

    // lists to hold agents and their respective steps
    private final HashMap<String, List<Agent>> agents = new HashMap<>();
    private final HashMap<String, List<AgentStep>> steps = new HashMap<>();

    // dependencies for the service
    private final ChatModel chatModel;
    private final Workspace workspace;
    private final List<Action> actions;
    private final ApplicationConfig applicationConfig;

    @Autowired
    public TaskService(ChatModel chatModel, Workspace workspace, List<Action> actions, ApplicationConfig applicationConfig) {
        this.chatModel = chatModel;
        this.workspace = workspace;
        this.actions = actions;
        this.applicationConfig = applicationConfig;
    }

    public Task plan(String username, String task, Map<String, String> additionalInputs) {
        ReActAgent agent = new ReActAgent(chatModel, workspace, actions, task, additionalInputs);
        if (applicationConfig.isLogPrompt()) {
            agent.enableLogging(applicationConfig.getLogFolder());
        }

        List<Agent> userAgents = agents.getOrDefault(username, new ArrayList<>());
        userAgents.add(agent);
        agents.put(username, userAgents);

        return Task.builder()
                .input(task)
                .context(additionalInputs)
                .taskId(agent.getId())
                .build();
    }

    @Async
    public ListenableFuture<AgentStep> asyncNextStep(String username, String task, String userInput, Map<String, String> additionalInputs) {
        return AsyncResult.forValue(nextStep(username, task, userInput, additionalInputs));
    }

    public AgentStep nextStep(String username, String taskId, String userInput, Map<String, String> context) {
        List<Agent> userAgents = agents.get(username);
        Optional<Agent> agent = userAgents.stream().filter(a -> a.getId().equals(taskId)).findFirst();

        if (!agent.isPresent()) {
            log.warn("Task with id {} not found", taskId);
            throw new TaskNotFoundException(taskId.toString());
        }

        AgentStep step = agent.get().executeNextStep(userInput, context);

        List<AgentStep> taskSteps = steps.getOrDefault(taskId, new ArrayList<>());
        taskSteps.add(step);
        steps.put(taskId, taskSteps);

        return step;
    }

    public List<Task> getTasks(String username) {
        List<Agent> userAgents = agents.getOrDefault(username, new ArrayList<>());
        return userAgents.stream()
                .sorted(Comparator.comparing(Agent::getStartTime))
                .map(agent -> getTask(username, agent.getId()))
                .collect(Collectors.toList());
    }

    public Task getTask(String username, String agentId) {
        Agent agent = getAgent(username, agentId);

        return Task.builder()
                .input(agent.getTask())
                .context(agent.getContext())
                .taskId(agentId)
                .build();
    }

    public List<AgentStep> getSteps(String username, String agentId) {
        Agent agent = getAgent(username, agentId);

        if (!steps.containsKey(agent.getId())) {
            return new ArrayList<>();
        }

        return steps.get(agentId);
    }

    private Agent getAgent(String username, String agentId) {
        List<Agent> userAgents = agents.getOrDefault(username, new ArrayList<>());
        Optional<Agent> agent = userAgents.stream().filter(a -> a.getId().equals(agentId)).findFirst();

        if (!agent.isPresent()) {
            throw new TaskNotFoundException(agentId);
        }

        return agent.get();
    }
}
