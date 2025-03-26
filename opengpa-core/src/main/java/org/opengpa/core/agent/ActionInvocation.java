package org.opengpa.core.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionInvocation {

        @JsonProperty(required = true)
        private String name;

        @JsonProperty(required = true)
        private Map<String, Object> parameters;
    }