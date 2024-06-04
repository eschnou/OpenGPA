package org.opengpa.core.workspace;

import org.opengpa.core.model.WorkspaceDocument;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class SimpleMemoryWorkspace implements Workspace {

    private final Map<String, List<WorkspaceDocument>> documents = new HashMap<>();

    private final Map<String, String> documentsContent = new HashMap();

    @Override
    public List<WorkspaceDocument> getDocuments(String agentId) {
        return documents.getOrDefault(agentId, new ArrayList<>());
    }

    @Override
    public Optional<WorkspaceDocument> getDocumentById(String agentId, String documentId) {
        List<WorkspaceDocument> agentDocs = documents.getOrDefault(agentId, new ArrayList<>());
        return agentDocs.stream()
                .filter(artifact -> artifact.getDocumentId().equals(documentId))
                .findFirst();
    }

    @Override
    public Optional<WorkspaceDocument> getDocumentByName(String agentId, String name) {
        List<WorkspaceDocument> agentDocs = documents.getOrDefault(agentId, new ArrayList<>());
        return agentDocs.stream()
                .filter(artifact -> artifact.getName().equals(name))
                .findFirst();
    }

    @Override
    public WorkspaceDocument addDocument(String workspaceId, String relativePath, String name, byte[] content, Map<String, String> metadata) {
        List<WorkspaceDocument> agentDocs = documents.getOrDefault(workspaceId, new ArrayList<>());

        WorkspaceDocument document = WorkspaceDocument.builder()
                .documentId(UUID.randomUUID().toString())
                .name(name)
                .relativePath(relativePath)
                .metadata(metadata)
                .build();

        String stringContent = new String(content, StandardCharsets.UTF_8);

        agentDocs.add(document);
        documents.put(workspaceId, agentDocs);
        documentsContent.put(document.getDocumentId(), stringContent);

        return document;
    }

    @Override
    public byte[] getDocumentContent(String workspaceId, String documentId) {
        return documentsContent.get(documentId).getBytes(StandardCharsets.UTF_8);
    }
}
