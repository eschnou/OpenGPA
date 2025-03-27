package org.opengpa.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.server.dto.CategoryInfoDTO;
import org.opengpa.server.service.ActionCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/configuration")
@Tag(name = "Configuration", description = "Endpoints for retrieving application configuration")
public class ConfigurationController {

    private final ActionCategoryService actionCategoryService;

    public ConfigurationController(ActionCategoryService actionCategoryService) {
        this.actionCategoryService = actionCategoryService;
    }

    @GetMapping("/actions")
    public ResponseEntity<Map<String, ActionCategoryInfo>> getActionsConfiguration() {
        Map<String, List<Action>> actionsByCategory = actionCategoryService.getActionsByCategory();
        Map<String, ActionCategoryInfo> result = new HashMap<>();
        
        // For each category, create an ActionCategoryInfo that includes category metadata and actions
        for (Map.Entry<String, List<Action>> entry : actionsByCategory.entrySet()) {
            String categoryName = entry.getKey();
            List<Action> categoryActions = entry.getValue();
            
            // Get category metadata
            CategoryInfoDTO categoryInfo = actionCategoryService.getCategoryInfoByName(categoryName);
            
            // Map actions to ActionInfo
            List<ActionInfo> actionInfos = categoryActions.stream()
                    .map(action -> new ActionInfo(action.getName(), action.getDescription()))
                    .collect(Collectors.toList());
            
            // Create combined result
            ActionCategoryInfo categoryResult = new ActionCategoryInfo(
                    categoryInfo.getName(),
                    categoryInfo.getDisplayName(),
                    categoryInfo.getDescription(),
                    categoryInfo.getIcon(),
                    actionInfos
            );
            
            result.put(categoryName, categoryResult);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/action-categories")
    public ResponseEntity<List<CategoryInfoDTO>> getActionCategories() {
        List<CategoryInfoDTO> categories = actionCategoryService.getCategoryInfo();
        return ResponseEntity.ok(categories);
    }

    private static class ActionInfo {
        private final String name;
        private final String description;

        public ActionInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
    
    private static class ActionCategoryInfo {
        private final String name;
        private final String displayName;
        private final String description;
        private final String icon;
        private final List<ActionInfo> actions;

        public ActionCategoryInfo(String name, String displayName, String description, 
                                 String icon, List<ActionInfo> actions) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.actions = actions;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }

        public List<ActionInfo> getActions() {
            return actions;
        }
    }
}