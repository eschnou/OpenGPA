package org.opengpa.core.action;

import org.opengpa.core.agent.Agent;
import org.opengpa.core.model.ActionParameter;
import org.opengpa.core.model.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="opengpa.actions", name="outputmessage", havingValue = "true", matchIfMissing = true)
public class OutputMessageAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(OutputMessageAction.class);

    @Override
    public String getName() {
        return "outputMessage";
    }

    @Override
    public String getDescription() {
        return "Output a message to the user, you MUST provide the message in the `message`argument.";
    }

    @Override
    public List<ActionParameter> getArguments() {
        return List.of(
                ActionParameter.from("message", "The message to output to the user, special characters should be escaped.")
        );
    }

    public ActionResult apply(Agent agent, Map<String, String> input) {
        log.debug("Outputmessage action message={}", input.get("message"));
        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .result("The message has been displayed to the user.")
                .output(input.get("message"))
                .summary("")
                .build();
    }
}
