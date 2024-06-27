package org.opengpa.server.exceptions;

public class DocumentNotFoundException extends RuntimeException {
    private final String artifactId;

    public DocumentNotFoundException(String artifactId) {
        this.artifactId = artifactId;
    }
}
