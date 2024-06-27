package org.opengpa.core.workspace;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Workspace {
    List<Document> getDocuments(String workspaceId);

    Optional<Document> getDocument(String workspaceId, String name);

    Document addDocument(String workspaceId, String name, byte[] content, Map<String, String> metadata);

    byte[] getDocumentContent(String workspaceId, String name);

    default Document addDocument(String workspaceId, String name, byte[] content) {
        return addDocument(workspaceId, name, content, Collections.emptyMap());
    }
}
