package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.ActionParameter;
import org.opengpa.core.model.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="queryllm", havingValue = "true", matchIfMissing = false)
public class QueryLLMAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(ReadFileAction.class);

    private final ChatClient chatClient;

    public QueryLLMAction(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String getName() {
        return "queryLLM";
    }

    @Override
    public String getDescription() {
        return "Query a Large Language Model with the given prompt.";
    }

    @Override
    public List<ActionParameter> getArguments() {
        return List.of(
                ActionParameter.from("prompt", "The prompt to submit to the large language model.")
        );
    }

    public ActionResult apply(Agent agent, Map<String, String> request) {
        log.debug("Querying LLM with prompt={}", request.get("prompt"));

        String prompt = request.get("prompt");
        Generation response = chatClient.call(new Prompt(prompt)).getResult();

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .output(response.getOutput().getContent())
                .message("I have queried the LLM with prompt=" + formatPrompt(prompt))
                .build();
    }

    private String formatPrompt(String prompt) {
        if (prompt.length() < 50) {
            return prompt;
        }

        return prompt.substring(0, 50) + "...";
    }
}