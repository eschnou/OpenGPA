package org.opengpa.server.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengpa.core.action.Action;
import org.opengpa.server.dto.CategoryInfoDTO;
import org.opengpa.server.service.ActionCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigurationControllerTest {

    private ConfigurationController configurationController;

    @Mock
    private ActionCategoryService actionCategoryService;

    @Mock
    private Action coreAction;

    @Mock
    private Action webAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        configurationController = new ConfigurationController(actionCategoryService);
        
        // Configure mock actions
        when(coreAction.getName()).thenReturn("core_action");
        when(coreAction.getDescription()).thenReturn("Core action description");
        when(coreAction.getCategory()).thenReturn("core");
        
        when(webAction.getName()).thenReturn("web_action");
        when(webAction.getDescription()).thenReturn("Web action description");
        when(webAction.getCategory()).thenReturn("web");
        
        // Configure category info mock responses
        when(actionCategoryService.getCategoryInfoByName("core")).thenReturn(
                CategoryInfoDTO.builder()
                        .name("core")
                        .displayName("Core Tools")
                        .description("Basic tools")
                        .icon("Settings")
                        .build()
        );
        
        when(actionCategoryService.getCategoryInfoByName("web")).thenReturn(
                CategoryInfoDTO.builder()
                        .name("web")
                        .displayName("Web Tools")
                        .description("Web browsing tools")
                        .icon("Globe")
                        .build()
        );
    }

    @Test
    void testGetActionCategories() {
        // Setup
        List<CategoryInfoDTO> categoryInfos = List.of(
                CategoryInfoDTO.builder()
                        .name("core")
                        .displayName("Core Tools")
                        .description("Basic tools")
                        .icon("Settings")
                        .build(),
                CategoryInfoDTO.builder()
                        .name("web")
                        .displayName("Web Tools")
                        .description("Web browsing tools")
                        .icon("Globe")
                        .build()
        );
        
        when(actionCategoryService.getCategoryInfo()).thenReturn(categoryInfos);
        
        // Execute
        ResponseEntity<List<CategoryInfoDTO>> response = configurationController.getActionCategories();
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        // Check that the returned categories match our mocks
        CategoryInfoDTO core = response.getBody().get(0);
        assertEquals("core", core.getName());
        assertEquals("Core Tools", core.getDisplayName());
        assertEquals("Basic tools", core.getDescription());
        assertEquals("Settings", core.getIcon());
        
        verify(actionCategoryService, times(1)).getCategoryInfo();
    }

    @Test
    void testGetActionsConfiguration() {
        // Setup
        Map<String, List<Action>> actionsByCategory = new HashMap<>();
        actionsByCategory.put("core", List.of(coreAction));
        actionsByCategory.put("web", List.of(webAction));
        
        when(actionCategoryService.getActionsByCategory()).thenReturn(actionsByCategory);
        
        // Execute
        ResponseEntity<?> response = configurationController.getActionsConfiguration();
        
        // Verify basic attributes
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify service calls were made
        verify(actionCategoryService, times(1)).getActionsByCategory();
        verify(actionCategoryService, times(1)).getCategoryInfoByName("core");
        verify(actionCategoryService, times(1)).getCategoryInfoByName("web");
    }
}