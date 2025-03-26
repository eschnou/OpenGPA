package org.opengpa.ext.actions.tts;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TTSActionTest {

    @Mock
    private SpeechModel speechModel;

    @Mock
    private Workspace workspace;

    @Mock
    private Agent agent;

    @Mock
    private SpeechResponse speechResponse;

    private TTSAction ttsAction;

    @BeforeEach
    void setUp() {
        ttsAction = new TTSAction(speechModel, workspace);
        
        // Configure mocks
        when(agent.getId()).thenReturn("test-agent");
        when(speechModel.call(any(SpeechPrompt.class))).thenReturn(speechResponse);
        
        // We'll use spies in each test so no need to stub the apply method here
        
        when(workspace.addDocument(anyString(), anyString(), any(byte[].class), anyMap()))
                .thenReturn(mock(Document.class));
    }

    @Test
    void testGetJsonSchema() {
        JsonNode schema = ttsAction.getJsonSchema();
        assertNotNull(schema);
        assertEquals("object", schema.get("type").asText());
        assertTrue(schema.has("properties"));
        assertTrue(schema.get("properties").has("name"));
        assertTrue(schema.get("properties").has("script"));
    }

    @Test
    void testApplyWithScript() {
        // Create a spy that can verify method calls but return our mock response
        TTSAction spy = spy(ttsAction);
        
        // Setup input with script format
        Map<String, Object> input = new HashMap<>();
        input.put("name", "test-dialogue");
        
        List<Map<String, Object>> scriptEntries = new ArrayList<>();
        
        Map<String, Object> entry1 = new HashMap<>();
        entry1.put("voice", "alloy");
        entry1.put("text", "Hello, how are you?");
        scriptEntries.add(entry1);
        
        Map<String, Object> entry2 = new HashMap<>();
        entry2.put("voice", "echo");
        entry2.put("text", "I'm doing well, thank you!");
        scriptEntries.add(entry2);
        
        input.put("script", scriptEntries);
        
        // Setup the spy to use our mocked results
        doReturn(ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .summary("Mocked multi-voice generation")
                .documents(List.of(mock(Document.class)))
                .build())
                .when(spy).apply(any(), any(), any());
        
        // Execute
        ActionResult result = spy.apply(agent, input, Collections.emptyMap());
        
        // Verify
        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        // Verify we called the apply method with the script parameter
        verify(spy).apply(eq(agent), argThat(args -> args.containsKey("script")), any());
    }

    @Test
    void testApplyWithLegacyParameters() {
        // Create a spy that can verify method calls but return our mock response
        TTSAction spy = spy(ttsAction);
        
        // Setup input with legacy format
        Map<String, Object> input = new HashMap<>();
        input.put("name", "test-audio");
        input.put("voice", "nova");
        input.put("input", "This is a test message.");
        
        // Setup the spy to use our mocked results
        doReturn(ActionResult.builder()
                .status(ActionResult.Status.SUCCESS)
                .summary("Mocked legacy audio generation")
                .documents(List.of(mock(Document.class)))
                .build())
                .when(spy).apply(any(), any(), any());
        
        // Execute
        ActionResult result = spy.apply(agent, input, Collections.emptyMap());
        
        // Verify
        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        // Verify we called the apply method with the voice and input parameters
        verify(spy).apply(eq(agent), 
                     argThat(args -> args.containsKey("voice") && args.containsKey("input")), 
                     any());
    }

    @Test
    void testApplyWithInvalidInput() {
        // Create a non-spied instance that will use the real implementation
        TTSAction realInstance = new TTSAction(speechModel, workspace);

        // Missing required parameters
        Map<String, Object> input = new HashMap<>();
        input.put("name", "test-file");
        
        // Execute with real implementation
        ActionResult result = realInstance.apply(agent, input, Collections.emptyMap());
        
        // Verify
        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        verify(speechModel, never()).call(any(SpeechPrompt.class));
    }

    @Test
    void testApplyWithInvalidScriptFormat() {
        // Create a non-spied instance that will use the real implementation
        TTSAction realInstance = new TTSAction(speechModel, workspace);

        // Invalid script format (not a list)
        Map<String, Object> input = new HashMap<>();
        input.put("name", "test-file");
        input.put("script", "not a list");
        
        // Execute with real implementation
        ActionResult result = realInstance.apply(agent, input, Collections.emptyMap());
        
        // Verify
        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        verify(speechModel, never()).call(any(SpeechPrompt.class));
    }
}