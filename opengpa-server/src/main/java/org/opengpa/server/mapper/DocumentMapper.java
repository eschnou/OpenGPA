package org.opengpa.server.mapper;

import org.opengpa.core.workspace.Document;
import org.opengpa.server.dto.DocumentDTO;

import java.util.HashMap;

public class DocumentMapper {

    public static DocumentDTO toDocumentDTO(Document document) {

        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setTaskId(document.getWorkspaceId());
        documentDTO.setFilename(document.getName());
        documentDTO.setMetadata(new HashMap<>(document.getMetadata()));

        return documentDTO;
    }
}