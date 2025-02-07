package org.opengpa.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "opengpa.server")
@Data
public class ApplicationConfig {
    boolean logPrompt = false;
    File logFolder;

    private boolean closedBeta;
    private List<String> inviteCodes;

}
