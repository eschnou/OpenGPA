package org.opengpa.ext.actions.tts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Input schema for the TTSAction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TTSInput {

    @JsonProperty(value = "name", required = true)
    @JsonPropertyDescription("A filename for the rendered output, the extension mp3 will be added automatically")
    private String name;

    @JsonProperty(value = "script", required = true)
    @JsonPropertyDescription("Array of script entries with voice and text")
    private List<TTSScriptEntry> script;
}