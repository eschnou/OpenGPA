package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class AskQuestionAction extends LegacyActionAdapter {

    private static final Logger log = LoggerFactory.getLogger(AskQuestionAction.class);
    public static final String NAME = "ask_question";

    public AskQuestionAction() {
        log.info("Creating AskQuestionAction");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Ask a question to the user, you MUST provide the question in the `message` argument.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("message", "The message to output to the user, special characters should be escaped.")
        );
    }

    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> input, Map<String, String> context) {
        log.debug("AskQuestionAction action message={}", input.get("message"));

        String message = input.get("message");
        if (!StringUtils.hasText(message)) {
            message = "";
        }
        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .result(message)
                .summary("The question has been displayed to the user.")
                .build();
    }
}
