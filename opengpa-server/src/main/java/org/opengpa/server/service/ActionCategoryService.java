package org.opengpa.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.core.action.Action;
import org.opengpa.mcp.McpActionProvider;
import org.opengpa.server.dto.CategoryInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing action categories and providing categorized actions.
 */
@Service
@Slf4j
public class ActionCategoryService {

    private final List<Action> actions;
    private final McpActionProvider mcpActionProvider;
    private final Map<String, CategoryInfoDTO> categoryConfigMap;

    @Autowired
    public ActionCategoryService(List<Action> actions, McpActionProvider mcpActionProvider) {
        this.actions = new ArrayList<>(actions);
        this.mcpActionProvider = mcpActionProvider;
        
        // Add MCP actions
        List<Action> mcpActions = mcpActionProvider.getMCPActions();
        if (mcpActions != null) {
            this.actions.addAll(mcpActions);
        }
        
        // Load category configuration
        this.categoryConfigMap = loadCategoryConfig();
    }

    /**
     * Loads category configuration from the JSON file.
     * 
     * @return Map of category name to CategoryInfo
     */
    private Map<String, CategoryInfoDTO> loadCategoryConfig() {
        Map<String, CategoryInfoDTO> result = new HashMap<>();
        
        try {
            ClassPathResource resource = new ClassPathResource("category-config.json");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(resource.getInputStream());
            JsonNode categoriesNode = rootNode.get("categories");
            
            List<CategoryInfoDTO> categories = mapper.convertValue(
                    categoriesNode, 
                    new TypeReference<List<CategoryInfoDTO>>() {}
            );
            
            // Create a map for faster lookup
            for (CategoryInfoDTO category : categories) {
                result.put(category.getName(), category);
            }
            
            log.info("Loaded {} categories from configuration", categories.size());
        } catch (IOException e) {
            log.error("Failed to load category configuration", e);
        }
        
        return result;
    }

    /**
     * Create default category info for unknown categories.
     * 
     * @param name The category name
     * @return Default category info
     */
    private CategoryInfoDTO getDefaultCategoryInfo(String name) {
        // Capitalize first letter for display name
        String displayName = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        return CategoryInfoDTO.builder()
                .name(name)
                .displayName(displayName)
                .description("")
                .icon("Gear")
                .build();
    }

    /**
     * Get all available action categories with their metadata.
     *
     * @return List of category info DTOs
     */
    public List<CategoryInfoDTO> getCategoryInfo() {
        Set<String> categoryNames = actions.stream()
                .map(Action::getCategory)
                .collect(Collectors.toSet());
        
        List<CategoryInfoDTO> result = new ArrayList<>();
        
        for (String categoryName : categoryNames) {
            CategoryInfoDTO info = categoryConfigMap.getOrDefault(categoryName, getDefaultCategoryInfo(categoryName));
            result.add(info);
        }
        
        // Sort by displayName
        result.sort(Comparator.comparing(CategoryInfoDTO::getDisplayName));
        
        return result;
    }

    /**
     * Get all available action categories.
     *
     * @return List of category names
     */
    public List<String> getCategories() {
        return actions.stream()
                .map(Action::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all actions grouped by category.
     *
     * @return Map of category name to list of actions
     */
    public Map<String, List<Action>> getActionsByCategory() {
        Map<String, List<Action>> result = new HashMap<>();
        
        for (Action action : actions) {
            String category = action.getCategory();
            List<Action> categoryActions = result.getOrDefault(category, new ArrayList<>());
            categoryActions.add(action);
            result.put(category, categoryActions);
        }
        
        return result;
    }

    /**
     * Get category information for a specific category.
     * 
     * @param categoryName The name of the category
     * @return CategoryInfoDTO with metadata
     */
    public CategoryInfoDTO getCategoryInfoByName(String categoryName) {
        return categoryConfigMap.getOrDefault(categoryName, getDefaultCategoryInfo(categoryName));
    }

    /**
     * Get all actions from the specified categories.
     * If categories is null or empty, returns ALL available actions.
     * The OutputMessageAction is always included regardless of categories.
     *
     * @param categories List of category names to include, or null/empty for all actions
     * @return List of actions from the specified categories, or all actions if categories is null/empty
     */
    public List<Action> getActionsByCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return actions;  // Return all actions if no categories specified
        }
        
        List<Action> selectedActions = actions.stream()
                .filter(action -> categories.contains(action.getCategory()) || 
                                  "output_message".equals(action.getName()))
                .collect(Collectors.toList());
        
        return selectedActions;
    }
}