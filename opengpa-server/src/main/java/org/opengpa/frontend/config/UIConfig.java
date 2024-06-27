package org.opengpa.frontend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "opengpa.ui")
public class UIConfig {

    String name = "OpenGPA";

}
