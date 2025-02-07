package org.opengpa.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.rag.service.RagChunk;
import org.opengpa.rag.service.RagDocument;
import org.opengpa.rag.service.RagService;
import org.opengpa.server.dto.RagChunkDTO;
import org.opengpa.server.dto.RagDocumentDTO;
import org.opengpa.server.mapper.RagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@Slf4j
@Tag(name = "Knowledge Management", description = "Endpoint for managing Agent knowledge (RAG)")
public class DocumentController {

    private final RagService ragService;

    @Autowired
    public DocumentController(RagService ragProvider) {
        this.ragService = ragProvider;
    }

    @GetMapping
    public ResponseEntity<List<RagDocumentDTO>> listDocuments(Principal principal) {
        List<RagDocumentDTO> documents = ragService.listDocuments(principal.getName()).stream().map(RagMapper::toRagDocumentDTO).toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<RagDocumentDTO> getDocument(Principal principal, @PathVariable String documentId) {
        return ragService.getDocument(documentId)
                .filter(ragDocument -> ragDocument.getUsername().equals(principal.getName()))
                .map(RagMapper::toRagDocumentDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{documentId}/chunks")
    public ResponseEntity<List<RagChunkDTO>> getChuinks(Principal principal, @PathVariable String documentId) {
        RagDocument document = ragService.getDocument(documentId)
                .filter(ragDocument -> ragDocument.getUsername().equals(principal.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<RagChunk> sortedChunks = document.getChunks().stream()
                .sorted(Comparator.comparingInt(RagChunk::getIndex))
                .collect(Collectors.toList());

        List<RagChunkDTO> dto = sortedChunks.stream().map(RagMapper::toRagChunkDTOSummary).toList();

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(Principal principal, @PathVariable String documentId) {
        RagDocument document = ragService.getDocument(documentId)
                .filter(ragDocument -> ragDocument.getUsername().equals(principal.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ragService.deleteDocument(document.getDocumentId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity ingestDocument(
            Principal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            byte[] content = file.getBytes();

            log.debug("ingestDocument username={} filename={}", principal.getName(), filename);
            RagDocument document = ragService.ingestDocument(principal.getName(), filename, contentType, content, title, description);
            return ResponseEntity.ok(RagMapper.toRagDocumentDTO(document));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}