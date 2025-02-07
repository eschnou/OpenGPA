package org.opengpa;

import org.opengpa.core.config.EnableOpenGPA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableOpenGPA
public class ApplicationServer {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationServer.class, args);
	}

}
