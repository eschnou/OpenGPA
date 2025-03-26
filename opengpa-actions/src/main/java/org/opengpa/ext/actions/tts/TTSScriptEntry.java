package org.opengpa.ext.actions.tts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an entry in a TTS script with voice and text content
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TTSScriptEntry {
    @JsonProperty(value = "voice", required = true)
    @JsonPropertyDescription("The voice to use for this segment (alloy, echo, fable, onyx, nova, shimmer)")
    private String voice;
    
    @JsonProperty(value = "text", required = true)
    @JsonPropertyDescription("The text to synthesize for this voice")
    private String text;
}