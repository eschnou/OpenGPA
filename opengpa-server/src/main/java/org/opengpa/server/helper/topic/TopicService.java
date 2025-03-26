package org.opengpa.server.helper.topic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opengpa.core.agent.react.ReActAgentOutput;
import org.opengpa.server.model.Task;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TopicService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatModel chatModel;

    private static final String PROMPT = """
            You are a topic summary service. You will receive a long list of chat interactions between a user and
            a system. You must attempt to find a common theme in the conversation and generate a short topic title 
            and a topic summary. Try to come up with a proper generalization and do not stay too close to the original 
            request.
            
            The following is a conversation:
            {conversation}
            
            You must answer using a properly json output containing:
            - title : a very short topic title (5 words maximum)
            - summary: a short summary of the discussion 
            - reasoning: explain why you decided this topic and summary
                        
            {format}
            """;

    public TopicService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Optional<TopicSummaryDTO> summarize(Task task) {
        // Prepare system prompt
        BeanOutputConverter<TopicSummaryDTO> outputConverter = new BeanOutputConverter<>(TopicSummaryDTO.class);

        PromptTemplate userPrompt = new PromptTemplate(PROMPT);
        Message userMessage = userPrompt.createMessage(
                Map.of("conversation", renderInteraction(task),
                        "format", outputConverter.getFormat()));

        // Prepare the final prompt and add model specific options
        ChatOptions chatOptions = getOptions();
        Prompt prompt = new Prompt(List.of(userMessage), chatOptions);

        // Query the LLM (our 'brain') to decide on route
        Generation response = chatModel.call(prompt).getResult();
        Optional<TopicSummaryDTO> topicOutput = parseResult(outputConverter, response);

        if (topicOutput.isPresent()) {
            log.debug("Generated new topic for task={} with title={} and reason={}", task.getTaskId(), topicOutput.get().getTitle(), topicOutput.get().getReasoning());
        }

        return topicOutput;
    }

    private OllamaOptions getOptions() {
        if (chatModel instanceof OllamaChatModel) {
            OllamaOptions ollamaOptions = new OllamaOptions();
            ollamaOptions.setFormat("json");
            return ollamaOptions;
        }

        return null;
    }

    private Optional<TopicSummaryDTO> parseResult(BeanOutputConverter<TopicSummaryDTO> outputConverter, Generation response) {
        if (response == null | response.getOutput() == null | Strings.isBlank(response.getOutput().getText())) {
            return Optional.empty();
        }

        // Some LLM might ignore the directive and enclose the json within ```json which is good enough
        String content = response.getOutput().getText();
        if (response.getOutput().getText().startsWith("```")) {
            Pattern pattern = Pattern.compile("```[a-z]*(.*)```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                content = matcher.group(1).trim(); // Got the string between "```json" and "```"
            }
        }

        // Attempt to parse the json to the corresponding ActionOutput
        try {
            return Optional.of(outputConverter.convert(content));
        } catch (Exception e) {
            log.debug("Failed at parsing agent ouput, error: {}", e.getMessage());
            return Optional.empty();
        }
    }

    String renderInteraction(Task task) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(task.getAgent().getSteps());
        } catch (JsonProcessingException e) {
            log.warn("Failed to render interactions", e);
            return "{}";
        }
    }
}
