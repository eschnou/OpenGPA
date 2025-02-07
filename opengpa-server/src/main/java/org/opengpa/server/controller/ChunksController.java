package org.opengpa.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.opengpa.rag.service.RagService;
import org.opengpa.server.dto.RagChunkDTO;
import org.opengpa.server.mapper.RagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/chunks")
@Slf4j
public class ChunksController {

    private final RagService ragService;

    @Autowired
    public ChunksController(RagService ragProvider) {
        this.ragService = ragProvider;
    }

    @GetMapping("/{chunkId}")
    public ResponseEntity<RagChunkDTO> getDocument(Principal principal, @PathVariable String chunkId) {
        return ragService.getChunk(chunkId)
                .filter(ragChunk -> ragChunk.getDocument().getUsername().equals(principal.getName()))
                .map(RagMapper::toRagChunkDTODetailed)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}