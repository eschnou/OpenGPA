package org.opengpa.core.action.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadFileActionTest {

    @Mock
    private Workspace workspace;

    @Mock
    private Agent agent;

    private ReadFileAction readFileAction;

    @BeforeEach
    void setUp() {
        readFileAction = new ReadFileAction(workspace);
    }

    @Test
    void testGetName() {
        assertEquals("readFile", readFileAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Read a file with the given filename.", readFileAction.getDescription());
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = readFileAction.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("filename", parameters.get(0).getName());
        assertEquals("Relative path of the file to read in the agent workspace", parameters.get(0).getDescription());
    }

    @Test
    void testApplyWithExistingFile() {
        String agentId = "agent1";
        String filename = "test.txt";
        String fileContent = "This is a test file.";
        Map<String, String> request = new HashMap<>();
        request.put("filename", filename);

        when(agent.getId()).thenReturn(agentId);
        when(workspace.getDocument(agentId, filename)).thenReturn(Optional.of(Document.builder().name(filename).metadata(Map.of("content-type","text/plain")).build()));
        when(workspace.getDocumentContent(agentId, filename)).thenReturn(fileContent.getBytes());

        ActionResult result = readFileAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals(fileContent, result.getResult());
        assertEquals("Successfully read file test.txt from the agent workspace.", result.getSummary());
        assertNull(result.getError());

        verify(workspace).getDocument(agentId, filename);
        verify(workspace).getDocumentContent(agentId, filename);
    }

    @Test
    void testApplyWithNonExistingFile() {
        String agentId = "agent1";
        String filename = "nonexistent.txt";
        Map<String, String> request = new HashMap<>();
        request.put("filename", filename);

        when(agent.getId()).thenReturn(agentId);
        when(workspace.getDocument(agentId, filename)).thenReturn(Optional.empty());

        ActionResult result = readFileAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertNull(result.getResult());
        assertEquals("I could not find the file nonexistent.txt in the workspace.", result.getSummary());
        assertEquals("File not found.", result.getError());

        verify(workspace).getDocument(agentId, filename);
        verify(workspace, never()).getDocumentContent(anyString(), anyString());
    }

    @Test
    void testApplyWithMissingFilename() {
        String agentId = "agent1";
        Map<String, String> request = new HashMap<>();

        when(agent.getId()).thenReturn(agentId);

        ActionResult result = readFileAction.apply(agent, request, Collections.emptyMap());

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        assertNull(result.getResult());
        assertEquals("I could not find the file null in the workspace.", result.getSummary());
        assertEquals("File not found.", result.getError());

        verify(workspace).getDocument(agentId, null);
        verify(workspace, never()).getDocumentContent(anyString(), anyString());
    }
}