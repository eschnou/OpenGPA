// SendEmailAction.java
package org.opengpa.ext.actions.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.agent.react.ReActAgentOutput;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "opengpa.actions.email", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SendEmailAction implements Action {

    public static final String ACTION_NAME = "send_email";

    private final Map<String, ActionResult> ongoingActions = new HashMap<>();
    private final SendEmailActionConfig emailConfig;
    private final ChatModel chatModel;

    private static final String PROMPT = """
            You are a helpful assistant writing emails on behalf of your manager. When being asked to write an email,
            you must generate an appropriate title and body.
                        
            You must write and sign in your own name, you are "{fromName}" the AI assistant.
            Your email address is "{fromAddress}".
            You must be polite and write with courtesy and empathy.
                        
            The following is the request of the manager:
            {request}
                        
            You must answer using a properly json output containing:
            - title : a short title for the email
            - body: the entire email body in plain text (no html)
            - reasoning: explain why you decided this title and body
                        
            {format}
            """;

    public SendEmailAction(SendEmailActionConfig emailConfig, ChatModel chatModel) {
        this.chatModel = chatModel;
        log.info("Creating EmailAction");
        this.emailConfig = emailConfig;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Send an email to a recipient.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("recipient", "The email address of the recipient"),
                ActionParameter.from("content", "Provide all content for the email, including recipient name, email body, etc.")
        );
    }

    @Override
    public boolean supportsStatefulExecution() {
        return true;
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, String> input, Map<String, String> context) {
        String recipient = input.get("recipient");
        String request = input.get("content");

        // Prepare system prompt
        BeanOutputConverter<EmailWriterResult> outputConverter = new BeanOutputConverter<>(EmailWriterResult.class);
        PromptTemplate userPrompt = new PromptTemplate(PROMPT);
        org.springframework.ai.chat.messages.Message userMessage = userPrompt.createMessage(
                Map.of("request", request,
                        "fromAddress", emailConfig.getFromAddress(),
                        "fromName", emailConfig.getFromName(),
                        "format", outputConverter.getFormat()));

        // Prepare the final prompt and add model specific options
        ChatOptions chatOptions = getOptions();
        Prompt prompt = new Prompt(List.of(userMessage), chatOptions);

        // Query the LLM (our 'brain') to decide on email content
        Generation response = chatModel.call(prompt).getResult();
        Optional<EmailWriterResult> generatedEmail = parseResult(outputConverter, response);

        // Verify we have proper content
        if (generatedEmail.isEmpty()) {
            log.error("Failed to generate email - the assistant could not generate the content.");
            return ActionResult.failed("Email generation failed", "Failed to generate email content");
        }

        // Create state data with the generated email content and recipient
        Map<String, String> stateData = new HashMap<>();
        stateData.put("recipient", recipient);
        stateData.put("subject", generatedEmail.get().getTitle());
        stateData.put("body", generatedEmail.get().getBody());

        // Return awaiting input state to get user confirmation
        ActionResult actionResult = ActionResult.awaitingInput(
                "Email draft prepared. Please review the subject and body before sending.",
                stateData
        );

        // Keep track of ongoing actions
        ongoingActions.put(actionResult.getActionId(), actionResult);

        return actionResult;
    }

    @Override
    public ActionResult continueAction(Agent agent, String actionId,
                                       Map<String, String> stateData, Map<String, String> context) {

        // Lookup ongoing action
        ActionResult ongoingAction = ongoingActions.get(actionId);
        if (ongoingAction == null) {
            log.error("The action '" + actionId + "' does not exist");
            return ActionResult.failed("The action '" + actionId + "' does not exist", "Attempted to continue an action that doesn't exist.");
        }

        // Extract data from state
        String recipient = stateData.getOrDefault("recipient", ongoingAction.getStateData().get("recipient"));
        String subject = stateData.getOrDefault("subject", ongoingAction.getStateData().get("subject"));
        String body = stateData.getOrDefault("body", ongoingAction.getStateData().get("body"));

        // Remove the ongoing action as we will finalize it
        ongoingActions.remove(ongoingAction.getActionId());

        // Send the email
        try {
            // Prepare the final body with reference ID
            StringBuilder emailBody = new StringBuilder();
            emailBody.append(body);
            emailBody.append(String.format("\nref:%s", agent.getId()));

            sendEmail(recipient, subject, emailBody.toString());

            // Prepare the final result data
            Map<String, String> result = new HashMap<>();
            result.put("recipient", recipient);
            result.put("subject", subject);
            result.put("body", body);

            return ActionResult.completed(result, "Email sent successfully to " + recipient);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            return ActionResult.failed(e.getMessage(), "Failed to send email");
        }
    }

    @Override
    public ActionResult cancelAction(Agent agent, String actionId) {
        ongoingActions.remove(actionId);
        return ActionResult.failed("Email sending cancelled", "The email sending process was cancelled");
    }

    private void sendEmail(String recipient, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", emailConfig.getSmtpHost());
        props.put("mail.smtp.port", emailConfig.getSmtpPort());

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getUsername(), emailConfig.getPassword());
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailConfig.getFromAddress()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

    private Optional<EmailWriterResult> parseResult(BeanOutputConverter<EmailWriterResult> outputConverter, Generation response) {
        if (response == null || response.getOutput() == null || Strings.isBlank(response.getOutput().getText())) {
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
            log.debug("Failed at parsing agent output, error: {}", e.getMessage());
            return Optional.empty();
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
}