package org.opengpa.server.service;

import org.opengpa.core.model.WorkspaceDocument;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.server.dto.Artifact;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArtifactService {

    private final Workspace workspace;

    public ArtifactService(Workspace workspace) {
        this.workspace = workspace;
    }

    public List<Artifact> getFiles(String agentId) {
        return workspace.getDocuments(agentId).stream().map(Artifact::fromWorkspaceDocument).toList();
    }

    public Optional<Artifact> getArtifactById(String workspaceId, String artifactId) {
        return workspace.getDocumentById(workspaceId, artifactId).map(Artifact::fromWorkspaceDocument);
    }

    public Optional<Artifact> getArtifactByName(String workspaceId, String filename) {
        return workspace.getDocumentById(workspaceId, filename).map(Artifact::fromWorkspaceDocument);
    }

    public Artifact addArtifact(String workspaceId, String relativePath, String filename, byte[] content, boolean agentCreated) {
        WorkspaceDocument workspaceDocument = workspace.addDocument(workspaceId, relativePath, filename, content, Map.of("userContent", "true"));
        return Artifact.fromWorkspaceDocument(workspaceDocument);
    }

    public byte[] getArtifactContent(String workspaceId, String artifactId) {
        return workspace.getDocumentContent(workspaceId, artifactId);
    }
}
