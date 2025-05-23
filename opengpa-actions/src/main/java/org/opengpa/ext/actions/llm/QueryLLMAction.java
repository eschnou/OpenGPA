package org.opengpa.ext.actions.llm;

import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.LegacyActionAdapter;
import org.opengpa.core.action.files.ReadFileAction;
import org.opengpa.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions.llm", name="enabled", havingValue = "true", matchIfMissing = false)
public class QueryLLMAction extends LegacyActionAdapter {

    private static final Logger log = LoggerFactory.getLogger(ReadFileAction.class);

    private final ChatModel chatModel;

    public QueryLLMAction(ChatModel chatModel) {
        log.info("Creating LLMAction");
        this.chatModel = chatModel;
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
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("prompt", "The prompt to submit to the large language model.")
        );
    }

    public ActionResult applyStringParams(Agent agent, Map<String, String> request, Map<String, String> context) {
        log.debug("Querying LLM with prompt={}", request.get("prompt"));

        String prompt = request.get("prompt");
        Generation response = chatModel.call(new Prompt(prompt)).getResult();

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .result(response.getOutput().getText())
                .summary("I have queried the LLM with prompt=" + formatPrompt(prompt))
                .build();
    }

    private String formatPrompt(String prompt) {
        if (prompt.length() < 50) {
            return prompt;
        }

        return prompt.substring(0, 50) + "...";
    }
}