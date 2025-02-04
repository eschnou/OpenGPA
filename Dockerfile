# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# Install Node.js
RUN apk add --update nodejs npm

# Set working directory
WORKDIR /build

# Copy the project files
COPY . .

# Build the application with production profile
RUN mvn clean package -Pproduction -DskipTests

# Runtime stage - using Playwright with chromium only
FROM mcr.microsoft.com/playwright/java:v1.42.0-jammy

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy pom files for Playwright installation
COPY --from=builder /build/pom.xml /app/
COPY --from=builder /build/opengpa-core/pom.xml /app/opengpa-core/
COPY --from=builder /build/opengpa-server/pom.xml /app/opengpa-server/
COPY --from=builder /build/opengpa-actions/pom.xml /app/opengpa-actions/
COPY --from=builder /build/opengpa-rag/pom.xml /app/opengpa-rag/

# Install Playwright browsers using Maven
RUN mvn -f /app/pom.xml exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

# Copy the built jar from the builder stage
COPY --from=builder /build/opengpa-server/target/opengpa-server-*.jar /app/opengpa-server.jar

# Create directory for logs
RUN mkdir -p /var/log/opengpa

# Set environment variable to skip installing other browsers
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

# Expose the application port
EXPOSE 8000

# Set environment variables
ENV JAVA_OPTS="-Xmx512m"

# Run the application
ENTRYPOINT ["java", "-jar", "/app/opengpa-server.jar"]