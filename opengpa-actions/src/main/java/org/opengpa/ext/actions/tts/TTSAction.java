package org.opengpa.ext.actions.tts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Component
@Slf4j
@ConditionalOnProperty(prefix="opengpa.actions.tts", name="enabled", havingValue = "true", matchIfMissing = false)
public class TTSAction implements Action {

    public static final String ACTION_NAME = "text_to_speech";
    private final SpeechModel speechModel;
    private final Workspace workspace;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TTSAction(SpeechModel speechModel, Workspace workspace) {
        log.info("Creating TTSAction");
        this.speechModel = speechModel;
        this.workspace = workspace;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Generate an MP3 file with speech from a script with multiple voices";
    }
    
    @Override
    public String getCategory() {
        return "speech";
    }

    @Override
    public JsonNode getJsonSchema() {
        return org.opengpa.core.action.JsonSchemaUtils.generateSchemaFromClass(TTSInput.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionResult apply(Agent agent, Map<String, Object> input, Map<String, String> context) {
        TTSInput ttsInput;
        try {
            ttsInput = objectMapper.convertValue(input, TTSInput.class);
        } catch (Exception e) {
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .result("Error parsing input parameters: " + e.getMessage())
                    .build();
        }

        List<TTSScriptEntry> scriptEntries = ttsInput.getScript();
        String filename = ttsInput.getName();

        try {
            byte[] audioData = generateMultiVoiceAudio(scriptEntries);
            
            // Create metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("format", "mp3");
            metadata.put("segments", String.valueOf(scriptEntries.size()));
            
            // Add the document to the workspace
            String fullFilename = filename + ".mp3";
            Document document = workspace.addDocument(agent.getId(), fullFilename, audioData, metadata);
            
            return ActionResult.builder()
                    .status(ActionResult.Status.SUCCESS)
                    .summary(String.format("Multi-voice text-to-speech completed successfully and saved to %s in the workspace.", fullFilename))
                    .result(String.format("Audio file generated with %d voice segments and saved to %s in the workspace.", 
                            scriptEntries.size(), fullFilename))
                    .documents(Collections.singletonList(document))
                    .build();
        } catch (Exception e) {
            log.error("Error generating text-to-speech audio", e);
            return ActionResult.builder()
                    .status(ActionResult.Status.FAILURE)
                    .result("Error generating text-to-speech audio: " + e.getMessage())
                    .build();
        }
    }

    private byte[] generateMultiVoiceAudio(List<TTSScriptEntry> scriptEntries) throws IOException {
        if (scriptEntries.isEmpty()) {
            throw new IllegalArgumentException("Script cannot be empty");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        for (int i = 0; i < scriptEntries.size(); i++) {
            log.debug("Generating multi-voice audio entry {}/{}.", i, scriptEntries.size());

            TTSScriptEntry entry = scriptEntries.get(i);
            OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                    .model("tts-1")
                    .voice(getVoiceFromInput(entry.getVoice()))
                    .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                    .speed(1.0f)
                    .build();

            SpeechPrompt speechPrompt = new SpeechPrompt(entry.getText(), speechOptions);
            SpeechResponse response = speechModel.call(speechPrompt);
            
            byte[] audioSegment = response.getResult().getOutput();
            outputStream.write(audioSegment);
        }
        
        return outputStream.toByteArray();
    }

    private OpenAiAudioApi.SpeechRequest.Voice getVoiceFromInput(String voice) {
        switch (voice.toLowerCase()) {
            case "alloy":
                return OpenAiAudioApi.SpeechRequest.Voice.ALLOY;
            case "echo":
                return OpenAiAudioApi.SpeechRequest.Voice.ECHO;
            case "fable":
                return OpenAiAudioApi.SpeechRequest.Voice.FABLE;
            case "onyx":
                return OpenAiAudioApi.SpeechRequest.Voice.ONYX;
            case "nova":
                return OpenAiAudioApi.SpeechRequest.Voice.NOVA;
            case "shimmer":
                return OpenAiAudioApi.SpeechRequest.Voice.SHIMMER;
            default:
                log.debug("Unsupported voice type: {}, using ALLOY instead", voice);
                return OpenAiAudioApi.SpeechRequest.Voice.ALLOY;
        }
    }
}