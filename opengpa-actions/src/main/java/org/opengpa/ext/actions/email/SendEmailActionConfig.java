package org.opengpa.ext.actions.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "opengpa.actions.email")
    public class SendEmailActionConfig {
        private String smtpHost;
        private int smtpPort;
        private String username;
        private String password;
        private String fromAddress;
        private String fromName;
    }