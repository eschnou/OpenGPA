package org.opengpa.ext.actions.tts;

import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionParameter;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@ConditionalOnProperty(prefix="opengpa.actions.tts", name="enabled", havingValue = "true", matchIfMissing = false)
public class TTSAction implements Action {

    public static final String ACTION_NAME = "text_to_speech";
    private final SpeechModel speechModel;

    private final Workspace workspace;

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
        return "Render a mp3 file based on a text content and a voice";
    }

    @Override
    public List<ActionParameter> getParameters() {
        return List.of(
                ActionParameter.from("input", "The text to generate audio for."),
                ActionParameter.from("name", "A filename for the rendered output, the extension mp3 will be added automatically."),
                ActionParameter.from("voice", "The voice to use when generating the audio. Supported voices are alloy, echo, fable, onyx, nova, and shimmer.")
        );
    }

    @Override
    public ActionResult apply(Agent agent, Map<String, String> input, Map<String, String> context) {
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .withModel("tts-1")
                .withVoice(getVoiceFromInput(input.get("voice")))
                .withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .withSpeed(1.0f)
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(input.get("input"), speechOptions);
        SpeechResponse response = speechModel.call(speechPrompt);

        Map<String, String> metadataStringMap = response.getMetadata().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

        byte[] responseAsBytes = response.getResult().getOutput();
        String filename = input.get("name") + ".mp3";

        Document document = workspace.addDocument(agent.getId(), filename, responseAsBytes, metadataStringMap);

        return ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .summary(String.format("Text-to-speech rendering completed successfully and saved to %s in the workspace.", filename))
                .result(String.format("Audio file generated and saved to %s in the workspace.", filename))
                .documents(Arrays.asList(document))
                .build();
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
                log.debug("Unsupported voice type: {}", voice);
                return OpenAiAudioApi.SpeechRequest.Voice.ALLOY;
        }
    }
}
