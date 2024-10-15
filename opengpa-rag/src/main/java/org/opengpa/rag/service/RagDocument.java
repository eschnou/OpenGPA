package org.opengpa.rag.service;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rag_documents")
@Data
@NoArgsConstructor
public class RagDocument {
    @Id
    private String documentId;
    private String username;
    private String filename;
    private String title;
    private String description;
    private String contentType;
    private float progress;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RagChunk> chunks = new ArrayList<>();
}
