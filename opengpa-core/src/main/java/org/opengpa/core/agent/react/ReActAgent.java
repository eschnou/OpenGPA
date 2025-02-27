package org.opengpa.core.agent.react;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.VisibleForTesting;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.ActionInvocation;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.agent.AgentStep;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ReActAgent implements Agent {

    private final Resource stepSystemPromptResource = new ClassPathResource("prompts/reactSystemPrompt.st");

    private final Resource stepUserPromptResource = new ClassPathResource("prompts/reactUserPrompt.st");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatModel chatModel;

    private final Workspace workspace;

    private final List<Action> availableActions;

    private final List<AgentStep> executedSteps = new ArrayList<>();

    private final String task;

    private final UUID uuid;

    private final Date startTime;

    private Map<String, String> context;

    private boolean logInteractions = false;

    private File logFolder;


    public ReActAgent(ChatModel chatModel, Workspace workspace, List<Action> availableActions, String task, Map<String, String> context) {
        this.chatModel = chatModel;
        this.workspace = workspace;
        this.availableActions = availableActions;
        this.task = task;
        this.context = context;
        this.uuid = UUID.randomUUID();
        this.startTime = Calendar.getInstance().getTime();
    }

    @Override
    public String getTask() {
        return task;
    }

    @Override
    public List<AgentStep> getSteps() {
        return executedSteps;
    }

    @Override
    public Map<String, String> getContext() {
        return context;
    }

    @Override
    public String getId() {
        return uuid.toString();
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public AgentStep executeNextStep(String userInput, Map<String, String> context) {
        // If this is not the first step and there is a userInput, we consider
        // this a 'feedback' on the last executed action and it to the previous step.
        if (userInput != null && !userInput.isEmpty() && !executedSteps.isEmpty()) {
            AgentStep lastStep = executedSteps.get(executedSteps.size() - 1);
            lastStep.setFeedback(userInput);
        }

        // The context is expected to be complete at all time, we thus update our current
        // context with the new one.
        context = updateContext(context);

        // Prepare the system prompt. This one contains non user/task specific
        // information such as the list of possible actions.
        PromptTemplate systemPrompt = new SystemPromptTemplate(stepSystemPromptResource);
        Message systemMessage = systemPrompt.createMessage(Map.of(
                "context", renderContext(context),
                "actions", renderTools(availableActions),
                "files", renderFiles(workspace.getDocuments(getId()))));

        // Prepare the user prompt. This one contains all user/task specific actions.
        var outputParser = new BeanOutputParser<>(ReActAgentOutput.class);
        PromptTemplate userPrompt = new PromptTemplate(stepUserPromptResource);
        Message userMessage = userPrompt.createMessage(
                Map.of("task", task,
                        "instructions", userInput,
                        "format", outputParser.getFormat(),
                        "history", renderPreviousSteps(executedSteps)));

        // Prepare the final prompt and add model specific options
        ChatOptions chatOptions = getOptions();
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), chatOptions);

        // Query the LLM (our 'brain') to decide on next action
        Generation response = chatModel.call(prompt).getResult();

        ReActAgentOutput agentOutput = parseNextAction(outputParser, response);
        logInteraction(systemMessage, userMessage, response.getOutput().getContent());

        // Execute the action requested by the LLM
        ActionResult result = executeAction(agentOutput);

        AgentStep step = AgentStep
                .builder()
                .input(userInput)
                .context(context)
                .result(result)
                .action(agentOutput.getAction())
                .isFinal(agentOutput.isFinal())
                .reasoning(agentOutput.getReasoning())
                .build();

        executedSteps.add(step);
        return step;
    }

    public void enableLogging(File logFolder) {
        logInteractions = true;
        this.logFolder = logFolder;
    }

    public void disableLogging() {
        logInteractions = false;
    }

    private void logInteraction(Message systemMessage, Message userMessage, String content) {
        if (!logInteractions) return;
        try {
            String agentId = getId();
            int stepCount = executedSteps.size();
            File agentDirectory = new File(logFolder + "/" + agentId);
            if (!agentDirectory.exists()) {
                agentDirectory.mkdirs();
            }
            File logFile = new File(agentDirectory, "log_" + stepCount + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
                writer.write("System Message: " + systemMessage.toString());
                writer.write(System.lineSeparator());
                writer.write("User Message: " + userMessage.toString());
                writer.write(System.lineSeparator());
                writer.write("Content: " + content);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("Failed to create log file", e);
        }
    }

    private OllamaOptions getOptions() {
        if (chatModel instanceof OllamaChatModel) {
            OllamaOptions ollamaOptions = new OllamaOptions();
            ollamaOptions.setFormat("json");
            return ollamaOptions;
        }

        return null;
    }

    private Map<String, String> updateContext(Map<String, String> context) {
        context.forEach((k, v) -> this.context.put(k, v));
        this.context.put("dayOfWeek", DayOfWeek.from(LocalDate.now()).name());
        this.context.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        this.context.put("time", java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return this.context;
    }

    private ReActAgentOutput parseNextAction(BeanOutputParser<ReActAgentOutput> outputParser, Generation response) {
        if (response == null | response.getOutput() == null | Strings.isBlank(response.getOutput().getContent())) {
            return emptyAction("");
        }

        // Some LLM might ignore the directive and enclose the json within ```json which is good enough
        String content = response.getOutput().getContent();
        if (response.getOutput().getContent().startsWith("```")) {
            Pattern pattern = Pattern.compile("```[a-z]*(.*)```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                content = matcher.group(1).trim(); // Got the string between "```json" and "```"
            }
        }

        // Attempt to parse the json to the corresponding ActionOutput
        try {
            return outputParser.parse(content);
        } catch (Exception e) {
            log.debug("Failed at parsing agent ouput, error: {}", e.getMessage());
            return emptyAction(response.getOutput().getContent());
        }
    }

    private ReActAgentOutput emptyAction(String content) {
        return ReActAgentOutput.builder()
                .action(ActionInvocation.builder()
                        .name("outputMessage")
                        .parameters(Map.of(
                                "message", content
                        ))
                        .build())
                .reasoning("The agent failed at invoking an action, here is the raw output.")
                .isFinal(false)
                .build();
    }

    @VisibleForTesting
    String renderPreviousSteps(List<AgentStep> steps) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(steps);
        } catch (JsonProcessingException e) {
            log.warn("Failed to render steps", e);
            return "[]";
        }
    }

    @VisibleForTesting
    String renderTools(List<Action> actions) {
        List<ActionDTO> actionsMap = new ArrayList<>();

        for (Action action : actions) {
            ActionDTO actionDTO = ActionDTO.builder()
                    .name(action.getName())
                    .description(action.getDescription())
                    .parameters(action.getParameters())
                    .data(action.getData(context))
                    .build();

            actionsMap.add(actionDTO);
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actionsMap);
        } catch (JsonProcessingException e) {
            log.warn("Failed to render tools", e);
            return "[]";
        }
    }

    @VisibleForTesting
    String renderContext(Map<String, String> context) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        } catch (JsonProcessingException e) {
            log.warn("Failed to render context", e);
            return "{}";
        }
    }

    @VisibleForTesting
    String renderFiles(List<Document> documents) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(documents);
        } catch (JsonProcessingException e) {
            log.warn("Failed to render documents", e);
            return "[]";
        }
    }

    private ActionResult executeAction(ReActAgentOutput output) {
        if (output.getAction() != null) {
            ActionInvocation action = output.getAction();
            Optional<Action> matchingAction = availableActions.stream().filter(a -> a.getName().equals(action.getName())).findFirst();
            if (matchingAction.isPresent()) {
                return matchingAction.get().apply(this, action.getParameters(), context);
            } else {
                return ActionResult.builder()
                        .summary(String.format("Agent invoked a non existent action {}", action.getName()))
                        .status(ActionResult.Status.FAILURE)
                        .error(String.format("The action '{}' does not exist, use only action available to you.", action.getName()))
                        .build();
            }
        }

        return ActionResult.builder()
                .summary("No action at this step.")
                .build();
    }
}
