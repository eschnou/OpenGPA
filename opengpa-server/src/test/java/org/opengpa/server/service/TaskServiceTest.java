package org.opengpa.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengpa.core.action.Action;
import org.opengpa.core.agent.Agent;
import org.opengpa.core.agent.react.ReActAgent;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.server.config.ApplicationConfig;
import org.opengpa.server.helper.topic.TopicService;
import org.opengpa.server.model.Task;
import org.springframework.ai.chat.model.ChatModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskService taskService;

    @Mock
    private ChatModel chatModel;

    @Mock
    private Workspace workspace;

    @Mock
    private TopicService topicService;

    @Mock
    private ActionCategoryService actionCategoryService;

    @Mock
    private ApplicationConfig applicationConfig;

    @Mock
    private Action coreAction;

    @Mock
    private Action webAction;

    @Mock
    private ReActAgent mockAgent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(applicationConfig.isLogPrompt()).thenReturn(false);
        
        // Configure mock actions
        when(coreAction.getName()).thenReturn("core_action");
        when(coreAction.getCategory()).thenReturn("core");
        
        when(webAction.getName()).thenReturn("web_action");
        when(webAction.getCategory()).thenReturn("web");
        
        // Configure action category service
        when(actionCategoryService.getActionsByCategories(isNull()))
                .thenReturn(List.of(coreAction, webAction));
        when(actionCategoryService.getActionsByCategories(anyList()))
                .thenAnswer(invocation -> {
                    List<String> categories = invocation.getArgument(0);
                    if (categories.contains("core") && !categories.contains("web")) {
                        return List.of(coreAction);
                    } else if (!categories.contains("core") && categories.contains("web")) {
                        return List.of(webAction);
                    } else if (categories.contains("core") && categories.contains("web")) {
                        return List.of(coreAction, webAction);
                    } else {
                        return List.of();
                    }
                });
        
        // For the test purposes, we'll just mock the behavior without trying to 
        // intercept the ReActAgent constructor
        mockAgent = mock(ReActAgent.class);
        when(mockAgent.getId()).thenReturn("test-agent-id");
        when(mockAgent.getTask()).thenReturn("Test task");
        
        taskService = new TaskService(chatModel, workspace, topicService, actionCategoryService, applicationConfig);
    }

    @Test
    void testPlanWithNoCategories() {
        // Execute
        Task task = taskService.plan("testuser", "Test task", new HashMap<>());
        
        // Verify
        assertNotNull(task);
        assertEquals("Test task", task.getTitle());
        assertNull(task.getEnabledCategories());
        
        // Verify that the actionCategoryService was called with null
        verify(actionCategoryService, times(1)).getActionsByCategories(isNull());
    }

    @Test
    void testPlanWithSelectedCategories() {
        // Setup
        List<String> enabledCategories = List.of("core");
        
        // Execute
        Task task = taskService.plan("testuser", "Test task", new HashMap<>(), enabledCategories);
        
        // Verify
        assertNotNull(task);
        assertEquals("Test task", task.getTitle());
        assertEquals(enabledCategories, task.getEnabledCategories());
        
        // Verify that the actionCategoryService was called with the correct categories
        verify(actionCategoryService, times(1)).getActionsByCategories(eq(enabledCategories));
    }

    @Test
    void testPlanWithMultipleSelectedCategories() {
        // Setup
        List<String> enabledCategories = List.of("core", "web");
        
        // Execute
        Task task = taskService.plan("testuser", "Test task", new HashMap<>(), enabledCategories);
        
        // Verify
        assertNotNull(task);
        assertEquals("Test task", task.getTitle());
        assertEquals(enabledCategories, task.getEnabledCategories());
        
        // Verify that the actionCategoryService was called with the correct categories
        verify(actionCategoryService, times(1)).getActionsByCategories(eq(enabledCategories));
    }
}