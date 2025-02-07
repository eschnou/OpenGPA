package org.opengpa.server.mapper;

import org.opengpa.rag.service.RagChunk;
import org.opengpa.rag.service.RagDocument;
import org.opengpa.server.dto.RagChunkDTO;
import org.opengpa.server.dto.RagDocumentDTO;

public class RagMapper {

    public static RagDocumentDTO toRagDocumentDTO(RagDocument document) {
        RagDocumentDTO dto = new RagDocumentDTO();
        dto.setId(document.getDocumentId());
        dto.setFilename(document.getFilename());
        dto.setTitle(document.getTitle());
        dto.setDescription(document.getDescription());
        dto.setContentType(document.getContentType());
        dto.setProgress(document.getProgress());
        return dto;
    }

    public static RagChunkDTO toRagChunkDTOSummary(RagChunk chunk) {
        RagChunkDTO dto = new RagChunkDTO();
        dto.setId(chunk.getChunkId());
        dto.setContent(chunk.getContent());
        return dto;
    }

    public static RagChunkDTO toRagChunkDTODetailed(RagChunk chunk) {
        RagChunkDTO dto = new RagChunkDTO();
        dto.setId(chunk.getChunkId());
        dto.setContent(chunk.getContent());
        dto.setDocumentId(chunk.getDocument().getDocumentId());
        dto.setDocumentTitle(chunk.getDocument().getTitle());
        dto.setDocumentDescription(chunk.getDocument().getDescription());
        return dto;
    }
}
