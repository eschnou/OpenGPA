package org.opengpa.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "opengpa.playwright")
@Data
public class PlaywrightConfig {
    public long timeout = 30000;
    public boolean headless = true;
}
