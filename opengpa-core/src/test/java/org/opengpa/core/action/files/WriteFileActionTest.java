package org.opengpa.core.action.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengpa.core.action.ActionParameter;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WriteFileActionTest {

    @Mock
    private Workspace workspace;

    @Mock
    private Agent agent;

    private WriteFileAction writeFileAction;

    @BeforeEach
    void setUp() {
        writeFileAction = new WriteFileAction(workspace);
    }

    @Test
    void testGetName() {
        assertEquals("writeFile", writeFileAction.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("Write a file with the given filename and body.", writeFileAction.getDescription());
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = writeFileAction.getParameters();
        assertEquals(2, parameters.size());
        assertEquals("filename", parameters.get(0).getName());
        assertEquals("Relative path of the file to write in the agent workspace", parameters.get(0).getDescription());
        assertEquals("body", parameters.get(1).getName());
        assertEquals("Text content to write to the file. Binary is not supported.", parameters.get(1).getDescription());
    }

    @Test
    void testApplyWithValidInput() {
        String agentId = "agent1";
        String filename = "test.txt";
        String fileContent = "This is a test file.";
        Map<String, String> request = new HashMap<>();
        request.put("filename", filename);
        request.put("body", fileContent);

        Document mockDocument = Document.builder().name(filename).metadata(Map.of("content-type", "text/plain")).build();

        when(agent.getId()).thenReturn(agentId);
        when(workspace.addDocument(eq(agentId), eq(filename), eq(fileContent.getBytes()), any())).thenReturn(mockDocument);

        ActionResult result = writeFileAction.apply(agent, request);

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals("The file test.txt has been written to the Agent workspace.", result.getSummary());
        assertEquals("The file test.txt has been written to the Agent workspace.", result.getResult());
        assertEquals(List.of(mockDocument), result.getDocuments());
        assertNull(result.getError());

        verify(workspace).addDocument(eq(agentId), eq(filename), eq(fileContent.getBytes()), any());
    }

    @Test
    void testApplyWithMissingFilename() {
        String agentId = "agent1";
        String fileContent = "This is a test file.";
        Map<String, String> request = new HashMap<>();
        request.put("body", fileContent);

        when(agent.getId()).thenReturn(agentId);

        ActionResult result = writeFileAction.apply(agent, request);

        assertEquals(ActionResult.Status.FAILURE, result.getStatus());
        verify(workspace, never()).addDocument(anyString(), anyString(), any(), any());

    }

    @Test
    void testApplyWithMissingBody() {
        String agentId = "agent1";
        String filename = "test.txt";
        Map<String, String> request = new HashMap<>();
        request.put("filename", filename);

        when(agent.getId()).thenReturn(agentId);

        assertThrows(NullPointerException.class, () -> writeFileAction.apply(agent, request));

        verify(workspace, never()).addDocument(anyString(), anyString(), any(), any());
    }

    @Test
    void testApplyWithEmptyBody() {
        String agentId = "agent1";
        String filename = "test.txt";
        String fileContent = "";
        Map<String, String> request = new HashMap<>();
        request.put("filename", filename);
        request.put("body", fileContent);

        Document mockDocument = Document.builder().name(filename).metadata(Map.of("content-type", "text/plain")).build();

        when(agent.getId()).thenReturn(agentId);
        when(workspace.addDocument(eq(agentId), eq(filename), eq(fileContent.getBytes()), any())).thenReturn(mockDocument);

        ActionResult result = writeFileAction.apply(agent, request);

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals("The file test.txt has been written to the Agent workspace.", result.getSummary());
        assertEquals("The file test.txt has been written to the Agent workspace.", result.getResult());
        assertEquals(List.of(mockDocument), result.getDocuments());
        assertNull(result.getError());

        verify(workspace).addDocument(eq(agentId), eq(filename), eq(fileContent.getBytes()), any());
    }
}