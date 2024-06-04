package org.opengpa.core.workspace;

import org.opengpa.core.model.WorkspaceDocument;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Workspace {
    List<WorkspaceDocument> getDocuments(String agentId);

    Optional<WorkspaceDocument> getDocumentById(String agentId, String documentId);

    Optional<WorkspaceDocument> getDocumentByName(String agentId, String name);

    WorkspaceDocument addDocument(String workspaceId, String relativePath, String name, byte[] content, Map<String, String> metadata);

    byte[] getDocumentContent(String workspaceId, String documentId);
}
