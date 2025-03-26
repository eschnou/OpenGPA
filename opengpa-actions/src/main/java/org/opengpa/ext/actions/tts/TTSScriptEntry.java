package org.opengpa.ext.actions.tts;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("voice")
    private String voice;
    
    @JsonProperty("text")
    private String text;
}