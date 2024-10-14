package org.opengpa.ext.actions.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.parser.BeanOutputParser;
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
        return "Ask the assistant to write an email";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("recipient", "The email address of the recipient"),
                ActionParameter.from("request", "Do not provide email body, instead provide a detailed " +
                        "request including purpose, recipient name, and any other information helpful for the assistant" +
                        "to write the email.")
        );
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, String> input, Map<String, String> context) {
        String recipient = input.get("recipient");
        String request = input.get("request");

        // Prepare system prompt
        var outputParser = new BeanOutputParser<>(EmailWriterResult.class);
        PromptTemplate userPrompt = new PromptTemplate(PROMPT);
        org.springframework.ai.chat.messages.Message userMessage = userPrompt.createMessage(
                Map.of("request", request,
                        "fromAddress", emailConfig.getFromAddress(),
                        "fromName", emailConfig.getFromName(),
                        "format", outputParser.getFormat()));

        // Prepare the final prompt and add model specific options
        ChatOptions chatOptions = getOptions();
        Prompt prompt = new Prompt(List.of(userMessage), chatOptions);

        // Query the LLM (our 'brain') to decide on email content
        Generation response = chatModel.call(prompt).getResult();
        Optional<EmailWriterResult> generatedEmail = parseResult(outputParser, response);

        // Verify we have proper content
        if (!generatedEmail.isPresent()) {
            log.error("Failed to send email - the assistant could not generate the content.");
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("Failed to send email")
                    .build();
        }

        // Prepare the final body
        StringBuilder emailBody = new StringBuilder();
        emailBody.append(generatedEmail.get().getBody());
        emailBody.append(String.format("\nref:%s", agent.getId()));

        // Send the email
        try {
            sendEmail(recipient, generatedEmail.get().getTitle(), emailBody.toString());
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .summary("Email sent successfully")
                    .result(prepareResult(recipient, generatedEmail.get().getTitle(), generatedEmail.get().getBody()))
                    .build();
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("Failed to send email")
                    .result(e.getMessage())
                    .build();
        }
    }

    private Map<String, String> prepareResult(String recipient, String title, String body) {
        Map<String, String> result = new HashMap<>();
        result.put("recipient", recipient);
        result.put("title", title);
        result.put("body", body);
        return result;
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

    private Optional<EmailWriterResult> parseResult(BeanOutputParser<EmailWriterResult> outputParser, Generation response) {
        if (response == null | response.getOutput() == null | Strings.isBlank(response.getOutput().getContent())) {
            return Optional.empty();
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
            return Optional.of(outputParser.parse(content));
        } catch (Exception e) {
            log.debug("Failed at parsing agent ouput, error: {}", e.getMessage());
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