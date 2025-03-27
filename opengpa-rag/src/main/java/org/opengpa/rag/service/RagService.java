package org.opengpa.rag.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.opengpa.rag.repository.RagChunkRepository;
import org.opengpa.rag.repository.RagDocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class RagService {

    public static final int CHUNK_SIZE = 1000;
    public static final int CHUNK_OVERLAP = 0;

    private final VectorStore vectorStore;
    private final RagDocumentRepository ragDocumentRepository;
    private final RagChunkRepository ragChunkRepository;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public RagService(VectorStore vectorStore, RagDocumentRepository ragDocumentRepository, RagChunkRepository ragChunkRepository, ApplicationEventPublisher eventPublisher) {
        this.vectorStore = vectorStore;
        this.ragDocumentRepository = ragDocumentRepository;
        this.ragChunkRepository = ragChunkRepository;
    }

    public List<RagDocument> listDocuments() {
        return ragDocumentRepository.findAll();
    }

    public List<RagDocument> listDocuments(String username) {
        return ragDocumentRepository.findByUsernameOrderByFilename(username);
    }

    public Optional<RagDocument> getDocument(String documentId) {
        return ragDocumentRepository.findById(documentId);
    }

    @Transactional
    public void deleteDocument(String documentId) {
        ragDocumentRepository.findById(documentId).ifPresent(document -> {
            vectorStore.delete(document.getChunks().stream()
                    .map(RagChunk::getChunkId)
                    .collect(Collectors.toList()));
            ragDocumentRepository.delete(document);
        });
    }

    public RagDocument ingestDocument(String username, String filename, String contentType, byte[] content, String title, String description) {
        String documentId = UUID.randomUUID().toString();
        log.debug("RagService - Processing document fileName={} documentId={}", filename, documentId);

        Map<String, Object> documentAttributes = new HashMap<>();
        documentAttributes.put("filename", filename);
        documentAttributes.put("title", title);
        documentAttributes.put("description", description);
        documentAttributes.put("contentType", contentType);
        documentAttributes.put("username", username);
        documentAttributes.put("documentId", documentId);

        String textContent = extractText(content, contentType);
        log.debug("RagService - Extracted content with length={}", textContent.length());

        List<Document> chunks = extractChunks(textContent, documentAttributes);
        log.debug("RagService - Generated {} chunks", chunks.size());

        RagDocument ragDocument = new RagDocument();
        ragDocument.setDocumentId(documentId);
        ragDocument.setFilename(filename);
        ragDocument.setContentType(contentType);
        ragDocument.setTitle(title);
        ragDocument.setDescription(description);
        ragDocument.setUsername(username);
        ragDocumentRepository.save(ragDocument);

        CompletableFuture.runAsync(() -> {
            try {
                indexChunks(chunks, ragDocument);
            } catch (Exception e) {
                log.error("Error during index chunks", e);
            }
        }, executorService);

        return ragDocument;
    }

    private void indexChunks(List<Document> chunks, RagDocument ragDocument) {
        int totalDocuments = chunks.size();
        int batchSize = 10;

        for (int i = 0; i < totalDocuments; i += batchSize) {
            List<Document> batch = chunks.subList(i, Math.min(i + batchSize, totalDocuments));
            vectorStore.add(batch);
            float progress = Math.min(i + batchSize, totalDocuments) / (float) totalDocuments;
            ragDocument.setProgress(progress);
            ragDocumentRepository.saveAndFlush(ragDocument);

            log.debug("RagService - Ingest chunk batch " + (i / batchSize + 1) + " of " + (int) Math.ceil((double) totalDocuments / batchSize));
        }

        ragDocument.setProgress((float) 1);
        ragDocumentRepository.saveAndFlush(ragDocument);

        List<RagChunk> chunkEntities = IntStream.range(0, chunks.size())
                .mapToObj(index -> {
                    Document d = chunks.get(index);
                    RagChunk chunk = new RagChunk();
                    chunk.setChunkId(d.getId());
                    chunk.setContent(d.getText());
                    chunk.setDocument(ragDocument);
                    chunk.setIndex(index);
                    return chunk;
                })
                .collect(Collectors.toList());

        ragDocument.setChunks(chunkEntities);
        ragDocumentRepository.saveAndFlush(ragDocument);

        log.debug("RagService - Processing of document {} complete", ragDocument.getDocumentId());
    }

    public List<RagDocument> getIncompleteDocuments(String username) {
        return ragDocumentRepository.findByUsernameAndProgressLessThan(username, 1.0f);
    }

    public List<Document> searchDocuments(String username, String query) {
        SearchRequest searchRequest = SearchRequest.builder().query(query).filterExpression(String.format("username == '%s'", username)).build();
        return vectorStore.similaritySearch(searchRequest);
    }

    private String extractText(byte[] content, String contentType) {
        if (contentType.equals("application/pdf")) {
            return parsePDF(content);
        } else if (contentType.equals("text/plain")) {
            return new String(content);
        } else {
            throw new UnsupportedOperationException("Unsupported content type: " + contentType);
        }
    }

    private List<Document> extractChunks(String textContent, Map<String, Object> documentAttributes) {
        List<Document> documents = new ArrayList<>();
        int position = 0;
        int chunkIndex = 0;

        while (position < textContent.length()) {
            // Determine the end of the current chunk
            int end = Math.min(position + CHUNK_SIZE, textContent.length());
            String chunk = textContent.substring(position, end);

            // Adjust chunk to end at a sentence if possible
            if (end < textContent.length()) {
                int lastPeriod = chunk.lastIndexOf('.');
                if (lastPeriod > CHUNK_SIZE / 2) {
                    end = position + lastPeriod + 1;
                    chunk = textContent.substring(position, end);
                }
            }

            // Create document with chunk
            Map<String, Object> chunkAttributes = new HashMap<>(documentAttributes);
            chunkAttributes.put("chunk_index", chunkIndex);

            Document doc = new Document(chunk.trim(), chunkAttributes);
            documents.add(doc);

            // Move to next chunk, ensuring progress
            position = Math.max(end - CHUNK_OVERLAP, position + 1);
            chunkIndex++;
        }

        return documents;
    }

    private String parsePDF(byte[] content) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to parse PDF", e);
            throw new RuntimeException("Failed to parse PDF", e);
        }
    }

    public Optional<RagChunk> getChunk(String chunkId) {
        return ragChunkRepository.findById(chunkId);
    }
}