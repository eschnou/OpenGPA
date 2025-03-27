package org.opengpa.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengpa.core.action.Action;
import org.opengpa.mcp.McpActionProvider;
import org.opengpa.server.dto.CategoryInfoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionCategoryServiceTest {

    private ActionCategoryService actionCategoryService;

    @Mock
    private McpActionProvider mcpActionProvider;

    @Mock
    private Action coreAction;

    @Mock
    private Action webAction;

    @Mock
    private Action filesystemAction;

    @Mock
    private Action mcpAction;

    @Mock
    private Action customAction;
    
    @Mock
    private Action outputMessageAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configure mock actions
        when(coreAction.getName()).thenReturn("core_action");
        when(coreAction.getDescription()).thenReturn("Core action description");
        when(coreAction.getCategory()).thenReturn("core");
        
        when(webAction.getName()).thenReturn("web_action");
        when(webAction.getDescription()).thenReturn("Web action description");
        when(webAction.getCategory()).thenReturn("web");
        
        when(filesystemAction.getName()).thenReturn("filesystem_action");
        when(filesystemAction.getDescription()).thenReturn("Filesystem action description");
        when(filesystemAction.getCategory()).thenReturn("filesystem");
        
        when(mcpAction.getName()).thenReturn("mcp_action");
        when(mcpAction.getDescription()).thenReturn("MCP action description");
        when(mcpAction.getCategory()).thenReturn("mcp_server");
        
        when(customAction.getName()).thenReturn("custom_action");
        when(customAction.getDescription()).thenReturn("Custom action description");
        when(customAction.getCategory()).thenReturn("custom_category");
        
        when(outputMessageAction.getName()).thenReturn("output_message");
        when(outputMessageAction.getDescription()).thenReturn("Output message description");
        when(outputMessageAction.getCategory()).thenReturn("core");
        
        // Configure MCP action provider
        List<Action> mcpActions = new ArrayList<>();
        mcpActions.add(mcpAction);
        when(mcpActionProvider.getMCPActions()).thenReturn(mcpActions);
        
        // Create list of core actions
        List<Action> coreActions = new ArrayList<>();
        coreActions.add(coreAction);
        coreActions.add(webAction);
        coreActions.add(filesystemAction);
        coreActions.add(customAction);
        coreActions.add(outputMessageAction);
        
        // Create the service
        actionCategoryService = new ActionCategoryService(coreActions, mcpActionProvider);
    }

    @Test
    void testGetCategories() {
        List<String> categories = actionCategoryService.getCategories();
        
        assertEquals(5, categories.size());
        assertTrue(categories.contains("core"));
        assertTrue(categories.contains("web"));
        assertTrue(categories.contains("filesystem"));
        assertTrue(categories.contains("mcp_server"));
        assertTrue(categories.contains("custom_category"));
    }

    @Test
    void testGetCategoryInfo() {
        List<CategoryInfoDTO> categoryInfos = actionCategoryService.getCategoryInfo();
        
        assertEquals(5, categoryInfos.size());
        
        // Find and check configured categories
        CategoryInfoDTO core = findCategoryByName(categoryInfos, "core");
        assertNotNull(core);
        assertEquals("Core Tools", core.getDisplayName());
        assertFalse(core.getDescription().isEmpty());
        assertEquals("Settings", core.getIcon());
        
        CategoryInfoDTO web = findCategoryByName(categoryInfos, "web");
        assertNotNull(web);
        assertEquals("Web Tools", web.getDisplayName());
        assertFalse(web.getDescription().isEmpty());
        assertEquals("Globe", web.getIcon());
        
        // Check default category info for the custom category
        CategoryInfoDTO custom = findCategoryByName(categoryInfos, "custom_category");
        assertNotNull(custom);
        assertEquals("Custom_category", custom.getDisplayName());
        assertEquals("", custom.getDescription());
        assertEquals("Gear", custom.getIcon());
    }
    
    private CategoryInfoDTO findCategoryByName(List<CategoryInfoDTO> categories, String name) {
        return categories.stream()
                .filter(category -> category.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Test
    void testGetActionsByCategory() {
        Map<String, List<Action>> actionsByCategory = actionCategoryService.getActionsByCategory();
        
        assertEquals(5, actionsByCategory.size());
        assertTrue(actionsByCategory.containsKey("core"));
        assertTrue(actionsByCategory.containsKey("web"));
        assertTrue(actionsByCategory.containsKey("filesystem"));
        assertTrue(actionsByCategory.containsKey("mcp_server"));
        assertTrue(actionsByCategory.containsKey("custom_category"));
        
        assertEquals(2, actionsByCategory.get("core").size());
        assertEquals(1, actionsByCategory.get("web").size());
        assertEquals(1, actionsByCategory.get("filesystem").size());
        assertEquals(1, actionsByCategory.get("mcp_server").size());
        assertEquals(1, actionsByCategory.get("custom_category").size());
        
        assertEquals("core_action", actionsByCategory.get("core").get(0).getName());
        assertEquals("web_action", actionsByCategory.get("web").get(0).getName());
        assertEquals("filesystem_action", actionsByCategory.get("filesystem").get(0).getName());
        assertEquals("mcp_action", actionsByCategory.get("mcp_server").get(0).getName());
        assertEquals("custom_action", actionsByCategory.get("custom_category").get(0).getName());
    }

    @Test
    void testGetCategoryInfoByName() {
        // Test known category
        CategoryInfoDTO core = actionCategoryService.getCategoryInfoByName("core");
        assertNotNull(core);
        assertEquals("core", core.getName());
        assertEquals("Core Tools", core.getDisplayName());
        assertFalse(core.getDescription().isEmpty());
        assertEquals("Settings", core.getIcon());
        
        // Test unknown category
        CategoryInfoDTO unknown = actionCategoryService.getCategoryInfoByName("unknown_category");
        assertNotNull(unknown);
        assertEquals("unknown_category", unknown.getName());
        assertEquals("Unknown_category", unknown.getDisplayName());
        assertEquals("", unknown.getDescription());
        assertEquals("Gear", unknown.getIcon());
    }

    @Test
    void testGetActionsByCategoriesWithFiltering() {
        List<String> enabledCategories = List.of("core", "web");
        List<Action> filteredActions = actionCategoryService.getActionsByCategories(enabledCategories);
        
        assertEquals(3, filteredActions.size());  // Core, web, and output_message (as part of core)
        
        // Verify correct actions were included
        boolean hasCore = false;
        boolean hasWeb = false;
        
        for (Action action : filteredActions) {
            if (action.getCategory().equals("core") && !action.getName().equals("output_message")) {
                hasCore = true;
            } else if (action.getCategory().equals("web")) {
                hasWeb = true;
            }
        }
        
        assertTrue(hasCore);
        assertTrue(hasWeb);
    }

    @Test
    void testGetActionsByCategoriesWithNullOrEmptyCategories() {
        // Expected total is 6 actions (5 core + 1 MCP)
        int expectedTotal = 6;
        
        // Test with null - should return ALL actions
        List<Action> allActionsWithNull = actionCategoryService.getActionsByCategories(null);
        assertEquals(expectedTotal, allActionsWithNull.size());
        
        // Verify each action is included
        assertTrue(allActionsWithNull.contains(coreAction));
        assertTrue(allActionsWithNull.contains(webAction));
        assertTrue(allActionsWithNull.contains(filesystemAction));
        assertTrue(allActionsWithNull.contains(mcpAction));
        assertTrue(allActionsWithNull.contains(customAction));
        assertTrue(allActionsWithNull.contains(outputMessageAction));
        
        // Test with empty list - should also return ALL actions
        List<Action> allActionsWithEmpty = actionCategoryService.getActionsByCategories(new ArrayList<>());
        assertEquals(expectedTotal, allActionsWithEmpty.size());
        
        // Verify each action is included
        assertTrue(allActionsWithEmpty.contains(coreAction));
        assertTrue(allActionsWithEmpty.contains(webAction));
        assertTrue(allActionsWithEmpty.contains(filesystemAction));
        assertTrue(allActionsWithEmpty.contains(mcpAction));
        assertTrue(allActionsWithEmpty.contains(customAction));
        assertTrue(allActionsWithEmpty.contains(outputMessageAction));
    }
    
    @Test
    void testOutputMessageActionAlwaysIncluded() {
        // Test with categories that don't include "core"
        List<String> nonCoreCategories = List.of("web", "filesystem");
        List<Action> filteredActions = actionCategoryService.getActionsByCategories(nonCoreCategories);
        
        // Expect web, filesystem, and output_message (even though it's in core category)
        assertEquals(3, filteredActions.size());
        
        // Verify output_message is included despite not including the core category
        boolean hasOutputMessage = filteredActions.stream()
                .anyMatch(action -> "output_message".equals(action.getName()));
        assertTrue(hasOutputMessage);
        
        // Test with a single category that's not core
        List<String> singleCategory = List.of("web");
        List<Action> singleCategoryActions = actionCategoryService.getActionsByCategories(singleCategory);
        
        // Expect web and output_message
        assertEquals(2, singleCategoryActions.size());
        
        // Verify output_message is included
        boolean hasOutputMessageSingle = singleCategoryActions.stream()
                .anyMatch(action -> "output_message".equals(action.getName()));
        assertTrue(hasOutputMessageSingle);
    }
}