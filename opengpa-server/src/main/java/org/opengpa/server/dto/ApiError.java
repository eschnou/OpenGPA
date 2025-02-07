package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private String path;
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors;  // For validation errors
}