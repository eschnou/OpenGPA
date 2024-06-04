package org.opengpa.server.exceptions;

public class ArtifactNotFoundException extends RuntimeException {
    private final String artifactId;

    public ArtifactNotFoundException(String artifactId) {
        this.artifactId = artifactId;
    }
}
