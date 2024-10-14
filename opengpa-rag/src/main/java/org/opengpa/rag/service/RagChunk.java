package org.opengpa.rag.service;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rag_chunks")
@Data
@NoArgsConstructor
public class RagChunk {
    @Id
    private String chunkId;

    private Integer index;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private RagDocument document;
}
