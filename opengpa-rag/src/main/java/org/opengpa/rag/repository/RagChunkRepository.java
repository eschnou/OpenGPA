package org.opengpa.rag.repository;

import org.opengpa.rag.service.RagChunk;
import org.opengpa.rag.service.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RagChunkRepository extends JpaRepository<RagChunk, String> {

}