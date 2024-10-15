package org.opengpa.rag.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.rag.service.RagChunk;
import org.opengpa.rag.service.RagDocument;
import org.opengpa.rag.service.RagService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "opengpa.actions", name = "rag", havingValue = "internal", matchIfMissing = false)
public class RagAction implements Action {

    public static final String NAME = "rag_search";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatModel chatModel;

    private final RagService ragService;

    private static final String PROMPT = """
            The following is a list of document chunks and a query from a user. You must try to answer the user query
            only using information provided by the chunks.
                        
            In your answer, every fact must be backed by a reference to one or multiple chunk.
            
            To add a reference you must put the corresponding chunk_id into brackets (e.g. [<uuid>]).    
                       
            If you cannot answer the question, explain what information is missing.
                        
            Question: %s
                        
            Chunks:
                        
            """;

    public RagAction(ChatModel chatModel, RagService ragService) {
        this.chatModel = chatModel;
        this.ragService = ragService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Search internal document store. Do not use this rag_search if you don't see a relevant document in the 'data' section below.";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("query", "The query to search for in the document store."),
                ActionParameter.from("keywords", "The keywords to use to find interesting document chunks.")
        );
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, String> input, Map<String, String> context) {
        String query = input.get("query");
        if (query == null || query.isEmpty()) {
            return errorResult("An error occurred while attempting to perform RAG search", "The query parameter is missing or has an empty value.");
        }

        String keywords = input.get("keywords");
        if (keywords == null || keywords.isEmpty()) {
            return errorResult("An error occurred while attempting to perform RAG search", "The keywords parameter is missing or has an empty value.");
        }

        log.debug("Searching RAG agent {} for keywords {}", agent.getId(), keywords);

        List<Document> chunks = ragService.searchDocuments(context.getOrDefault("username", null), query);
        List<Map<String, String>> chunksDTO = chunkDTO(chunks);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(PROMPT, query));
        stringBuilder.append(renderChunks(chunksDTO));

        String prompt = stringBuilder.toString();
        Generation response = chatModel.call(new Prompt(prompt)).getResult();

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .result(prepareResponse(response.getOutput().getContent()))
                .summary(String.format("Searched internal documents for '" + query + "'"))
                .build();
    }

    private RagActionResult prepareResponse(String content) {
        RagActionResult result = new RagActionResult();
        result.setContent(content);
        result.setChunks(extractReferences(content));
        return result;
    }

    private List<RagActionChunkResult> extractReferences(String content) {
        // First we extract all UUIDs
        Set<String> uniqueUUIDs = new HashSet<>();
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String uuidString = matcher.group();
            uniqueUUIDs.add(uuidString);
        }

        // Collect all chunks
        return uniqueUUIDs.stream()
                .map(ragService::getChunk)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::mapChunkToResult)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getData(Map<String, String> content) {
        return Map.of("documents", renderRagDocuments(content.getOrDefault("username", null)));
    }

    private List<Map<String, String>> renderRagDocuments(String username) {
        if (ragService == null) return Collections.emptyList();

        List<Map<String, String>> documents = new ArrayList<>();

        for (RagDocument ragDocument : ragService.listDocuments(username)) {
            Map<String, String> document = new HashMap<>();
            document.put("filename", ragDocument.getFilename());
            document.put("title", ragDocument.getTitle());
            document.put("description", ragDocument.getDescription());
            documents.add(document);
        }

        return documents;
    }

    private static ActionResult errorResult(String userMessage, String errorMessage) {
        return ActionResult.builder()
                .status(ActionResult.Status.FAILURE)
                .summary(userMessage)
                .error(errorMessage)
                .build();
    }

    private List<Map<String, String>> chunkDTO(List<Document> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    Map<String, String> d = new HashMap<>();
                    d.put("content", chunk.getContent());
                    d.put("chunk_id", String.valueOf(chunk.getId()));
                    return d;
                }).toList();
    }

    private String renderChunks(List<Map<String, String>> chunks) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(chunks);
        } catch (JsonProcessingException e) {
            log.warn("Failed to render chunks", e);
            return "[]";
        }
    }

    private RagActionChunkResult mapChunkToResult(RagChunk chunk) {
        RagActionChunkResult result = new RagActionChunkResult();
        result.setId(chunk.getChunkId());
        result.setDocumentId(chunk.getDocument().getDocumentId());
        result.setDocumentTitle(chunk.getDocument().getTitle());
        result.setDocumentDescription(chunk.getDocument().getDescription());
        result.setContent(chunk.getContent());
        return result;
    }
}
