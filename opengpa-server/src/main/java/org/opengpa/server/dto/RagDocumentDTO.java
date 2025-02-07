package org.opengpa.server.dto;

import lombok.Data;

@Data
public class RagDocumentDTO {
        private String id;
        private String filename;
        private String title;
        private String description;
        private String contentType;
        private float progress;
}

