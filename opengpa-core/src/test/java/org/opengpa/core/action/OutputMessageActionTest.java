package org.opengpa.core.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengpa.core.agent.Agent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OutputMessageActionTest {

    private OutputMessageAction outputMessageAction;

    @Mock
    private Agent mockAgent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        outputMessageAction = new OutputMessageAction();
    }

    @Test
    void testGetName() {
        assertEquals("output_message", outputMessageAction.getName());
    }

    @Test
    void testGetDescription() {
        assertTrue(outputMessageAction.getDescription().contains("Output a message to the user"));
    }

    @Test
    void testGetParameters() {
        List<ActionParameter> parameters = outputMessageAction.getParameters();
        assertEquals(1, parameters.size());
        ActionParameter parameter = parameters.get(0);
        assertEquals("message", parameter.getName());
        assertTrue(parameter.getDescription().contains("The message to output to the user"));
    }

    @Test
    void testApply() {
        Map<String, Object> input = new HashMap<>();
        String testMessage = "Test message";
        input.put("message", testMessage);

        ActionResult result = outputMessageAction.apply(mockAgent, input, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals(testMessage, result.getResult());
        assertEquals("The message has been displayed to the user.", result.getSummary());
    }

    @Test
    void testApplyWithEmptyMessage() {
        Map<String, Object> input = new HashMap<>();
        input.put("message", "");

        ActionResult result = outputMessageAction.apply(mockAgent, input, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals("", result.getResult());
        assertEquals("The message has been displayed to the user.", result.getSummary());
    }

    @Test
    void testApplyWithNullMessage() {
        Map<String, Object> input = new HashMap<>();
        input.put("message", null);

        ActionResult result = outputMessageAction.apply(mockAgent, input, Collections.emptyMap());

        assertEquals(ActionResult.Status.SUCCESS, result.getStatus());
        assertEquals("", result.getResult());
        assertEquals("The message has been displayed to the user.", result.getSummary());
    }
}