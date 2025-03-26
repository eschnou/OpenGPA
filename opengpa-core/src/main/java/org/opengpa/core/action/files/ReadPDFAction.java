package org.opengpa.core.action.files;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.action.LegacyActionAdapter;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@ConditionalOnProperty(prefix="opengpa.actions.files", name="enabled", havingValue = "true", matchIfMissing = false)
public class ReadPDFAction extends LegacyActionAdapter {

    public static final String ACTION_NAME = "readPDF";
    private final ChatModel chatModel;
    private final Workspace workspace;

    private static final String PROMPT = """
            The following is a PDF file content. Using this content, try to answer the question below. 
            
            In addition, also provide:
             - all metadata from the document such as title, author, organization, date, etc...
             - a summary of the document
             - any relevant http links that are present in the document
             
             Use markdown to structure your output.
            
            Question: 
            %s
            
            Content: 
            %s
           
            """;

    public ReadPDFAction(ChatModel chatModel, Workspace workspace) {
        log.info("Creating ReadPDFAction");
        this.chatModel = chatModel;
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Read a PDF file, convert it to text, and query an LLM about its content.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("file", "Relative path of the PDF file to read from the agent workspace"),
                ActionParameter.from("query", "The question or request about the PDF content")
        );
    }

    @Override
    public ActionResult applyStringParams(Agent agent, Map<String, String> request, Map<String, String> context) {
        String filename = request.get("file");
        String query = request.get("query");
        
        log.debug("Reading PDF {} with query '{}' for agent {}", filename, query, agent.getId());
        
        // Check if file exists in workspace
        Optional<Document> document = workspace.getDocument(agent.getId(), filename);
        if (document.isEmpty()) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary(String.format("I could not find the PDF file %s in the workspace.", filename))
                    .error("PDF file not found.")
                    .build();
        }
        
        // File extension check
        if (!filename.toLowerCase().endsWith(".pdf")) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary(String.format("The file %s is not a PDF file.", filename))
                    .error("Not a PDF file.")
                    .build();
        }
        
        byte[] pdfContent = workspace.getDocumentContent(agent.getId(), filename);
        
        try {
            // Extract text from PDF
            String extractedText = parsePDF(pdfContent);
            
            if (extractedText.trim().isEmpty()) {
                return ActionResult.builder()
                        .status(ActionResult.Status.FAILURE)
                        .summary(String.format("The PDF file %s appears to be empty or could not be parsed.", filename))
                        .error("Empty or unparseable PDF.")
                        .build();
            }
            
            // Prepare context for LLM with the PDF content and query
            String promptText = String.format(
                    PROMPT,
                    query, 
                    extractedText
            );
            
            // Create message and call the LLM
            Message userMessage = new UserMessage(promptText);
            Generation response = chatModel.call(new Prompt(List.of(userMessage))).getResult();
            
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .result(response.getOutput().getText())
                    .summary(String.format("Successfully read PDF %s and answered query about its content.", filename))
                    .build();
            
        } catch (Exception e) {
            log.error("Error processing PDF", e);
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .summary("An error occurred while processing the PDF file")
                    .error("Failed to process the PDF: " + e.getMessage())
                    .build();
        }
    }
    
    protected String parsePDF(byte[] content) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}