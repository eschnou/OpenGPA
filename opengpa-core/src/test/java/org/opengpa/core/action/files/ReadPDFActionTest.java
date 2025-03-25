package org.opengpa.core.action.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadPDFActionTest {

    @Mock
    private Workspace workspace;

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private ReadPDFAction readPDFAction;

    @Test
    void testGetName() {
        assertEquals("readPDF", readPDFAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Read a PDF file, convert it to text, and query an LLM about its content.", readPDFAction.getDescription());
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = readPDFAction.getParameters();
        assertEquals(2, parameters.size());
        assertEquals("file", parameters.get(0).getName());
        assertEquals("Relative path of the PDF file to read from the agent workspace", parameters.get(0).getDescription());
        assertEquals("query", parameters.get(1).getName());
        assertEquals("The question or request about the PDF content", parameters.get(1).getDescription());
    }

    @Test
    void testApplyWithNonExistingFile() {
        // Setup
        Agent agent = mock(Agent.class);
        String agentId = "agent1";
        String filename = "nonexistent.pdf";
        String query = "What is this document about?";
        
        Map<String, String> request = new HashMap<>();
        request.put("file", filename);
        request.put("query", query);

        // Mock behavior
        when(agent.getId()).thenReturn(agentId);
        when(workspace.getDocument(agentId, filename)).thenReturn(Optional.empty());

        // Execute
        ActionResult result = readPDFAction.apply(agent, request, Collections.emptyMap());

        // Verify
        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertNull(result.getResult());
        assertEquals("I could not find the PDF file nonexistent.pdf in the workspace.", result.getSummary());
        assertEquals("PDF file not found.", result.getError());

        verify(workspace).getDocument(agentId, filename);
        verify(workspace, never()).getDocumentContent(anyString(), anyString());
    }

    @Test
    void testApplyWithNonPDFFile() {
        // Setup
        Agent agent = mock(Agent.class);
        String agentId = "agent1";
        String filename = "document.txt";
        String query = "What is this document about?";
        
        Map<String, String> request = new HashMap<>();
        request.put("file", filename);
        request.put("query", query);

        // Mock behavior
        when(agent.getId()).thenReturn(agentId);
        when(workspace.getDocument(agentId, filename)).thenReturn(
            Optional.of(Document.builder().name(filename).metadata(Map.of("content-type","text/plain")).build())
        );

        // Execute
        ActionResult result = readPDFAction.apply(agent, request, Collections.emptyMap());

        // Verify
        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertNull(result.getResult());
        assertEquals("The file document.txt is not a PDF file.", result.getSummary());
        assertEquals("Not a PDF file.", result.getError());

        verify(workspace).getDocument(agentId, filename);
        verify(workspace, never()).getDocumentContent(anyString(), anyString());
    }
}