package org.opengpa.server.service;

import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.server.config.ApplicationConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class WorkspaceService implements Workspace {

    private final Map<String, List<Document>> documents = new HashMap<>();

    private final Map<String, byte[]> documentsContent = new HashMap();

    private final ApplicationConfig applicationConfig;

    public WorkspaceService(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public List<Document> getDocuments(String workspaceId) {
        return documents.getOrDefault(workspaceId, new ArrayList<>());
    }

    @Override
    public Optional<Document> getDocument(String workspaceId, String name) {
        List<Document> agentDocs = documents.getOrDefault(workspaceId, new ArrayList<>());
        return agentDocs.stream()
                .filter(artifact -> artifact.getName().equals(name))
                .findFirst();
    }

    @Override
    public Document addDocument(String workspaceId, String name, byte[] content, Map<String, String> metadata) {
        List<Document> agentDocs = documents.getOrDefault(workspaceId, new ArrayList<>());

        Document document = Document.builder()
                .name(name)
                .workspaceId(workspaceId)
                .metadata(metadata)
                .build();

        // If prompt logging is enabled we also save the documents
        if (applicationConfig.isLogPrompt()) {
            File agentDirectory = new File(applicationConfig.getLogFolder() + "/" + workspaceId);
            if (!agentDirectory.exists()) {
                agentDirectory.mkdirs();
            }

            try {
                Files.write(Paths.get(agentDirectory.getPath(), name), content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        agentDocs.add(document);
        documents.put(workspaceId, agentDocs);
        documentsContent.put(getDocumentKey(workspaceId, name), content);

        return document;
    }

    @Override
    public byte[] getDocumentContent(String workspaceId, String name) {
        List<Document> agentDocs = documents.getOrDefault(workspaceId, new ArrayList<>());
        Optional<Document> document = agentDocs.stream()
                .filter(artifact -> artifact.getName().equals(name))
                .findFirst();

        if (document.isPresent()) {
            return documentsContent.get(getDocumentKey(workspaceId, name));
        } else {
            return null;
        }
    }

    private String getDocumentKey(String workspaceId, String name) {
        return workspaceId + "-" + name;
    }
}
