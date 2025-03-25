package org.opengpa.core.action.files;

import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@ConditionalOnProperty(prefix="opengpa.actions.files", name="enabled", havingValue = "true", matchIfMissing = false)
public class AnalyzePictureAction implements Action {

    public static final String ACTION_NAME = "analyzePicture";
    private final ChatModel chatModel;
    private final Workspace workspace;

    public AnalyzePictureAction(ChatModel chatModel, Workspace workspace) {
        log.info("Creating AnalyzePictureAction");
        this.chatModel = chatModel;
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Analyze an image file with a multimodal LLM to answer a query about the image content.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("filename", "Relative path of the image file to analyze in the agent workspace"),
                ActionParameter.from("query", "The question or request about what to analyze in the image")
        );
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, String> request, Map<String, String> context) {
        String filename = request.get("filename");
        String query = request.get("query");
        
        log.debug("Analyzing image {} with query '{}' for agent {}", filename, query, agent.getId());
        
        Optional<Document> document = workspace.getDocument(agent.getId(), filename);
        if (document.isEmpty()) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary(String.format("I could not find the image file %s in the workspace.", filename))
                    .error("Image file not found.")
                    .build();
        }
        
        byte[] imageContent = workspace.getDocumentContent(agent.getId(), filename);
        
        try {
            // Determine MIME type from filename extension
            MimeType mimeType = getMimeTypeFromFilename(filename);
            
            // Create a ByteArrayResource from the image content
            Resource imageResource = new ByteArrayResource(imageContent);
            
            // Create media content with the resource
            Media imageMedia = new Media(mimeType, imageResource);
            
            // Create a multimodal message with the query and image
            Message userMessage = new UserMessage(query, imageMedia);
            
            // Call the LLM with the image and query
            Generation response = chatModel.call(new Prompt(List.of(userMessage))).getResult();
            
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .result(response.getOutput().getContent())
                    .summary(String.format("Successfully analyzed image %s with query: %s", filename, formatQuery(query)))
                    .build();
            
        } catch (Exception e) {
            log.error("Error analyzing image", e);
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("An error occurred while analyzing the image")
                    .error("Failed to process the image: " + e.getMessage())
                    .build();
        }
    }
    
    private MimeType getMimeTypeFromFilename(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        return switch (extension) {
            case "jpg", "jpeg" -> MimeType.valueOf("image/jpeg");
            case "png" -> MimeType.valueOf("image/png");
            case "gif" -> MimeType.valueOf("image/gif");
            case "webp" -> MimeType.valueOf("image/webp");
            case "bmp" -> MimeType.valueOf("image/bmp");
            default -> MimeType.valueOf("image/jpeg"); // Default to jpeg if unknown
        };
    }
    
    private String formatQuery(String query) {
        if (query.length() < 50) {
            return query;
        }
        
        return query.substring(0, 50) + "...";
    }
}