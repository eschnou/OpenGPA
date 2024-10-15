package org.opengpa.rag.repository;

import org.opengpa.rag.service.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RagDocumentRepository extends JpaRepository<RagDocument, String> {

    public List<RagDocument> findByUsernameOrderByFilename(String userId);
    List<RagDocument> findByUsernameAndProgressLessThan(String username, float progress);

}