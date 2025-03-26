package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class OutputMessageAction extends LegacyActionAdapter {

    private static final Logger log = LoggerFactory.getLogger(OutputMessageAction.class);
    public static final String NAME = "output_message";

    public OutputMessageAction() {
        log.info("Creating OutputMessageAction");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Output a message to the user, you MUST provide the message in the `message`argument.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("message", "The message to output to the user, special characters should be escaped.")
        );
    }

    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> input, Map<String, String> context) {
        log.debug("Outputmessage action message={}", input.get("message"));

        String message = input.get("message");
        if (!StringUtils.hasText(message)) {
            message = "";
        }
        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .result(message)
                .summary("The message has been displayed to the user.")
                .build();
    }
}
