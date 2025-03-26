package org.opengpa.core.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility class for working with JSON schema in the context of Actions
 */
public class JsonSchemaUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonSchemaUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(mapper);
    private static final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    /**
     * Generate a JSON schema from an action's parameter list
     * This provides backward compatibility for actions that haven't been updated to use JSON Schema
     * 
     * @param parameters List of ActionParameter objects
     * @return JsonNode representing the JSON schema
     */
    public static JsonNode generateSchemaFromParameters(List<ActionParameter> parameters) {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.put("title", "Action Parameters");
        
        ObjectNode properties = mapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ArrayNode required = mapper.createArrayNode();
        
        for (ActionParameter param : parameters) {
            ObjectNode property = mapper.createObjectNode();
            property.put("type", "string");
            property.put("description", param.getDescription());
            properties.set(param.getName(), property);
            required.add(param.getName());
        }
        
        schema.set("properties", properties);
        schema.set("required", required);
        
        return schema;
    }
    
    /**
     * Generate a JSON schema from a Java class
     * Useful for creating schemas for complex input types
     *
     * @param clazz The class to generate schema from
     * @return JsonNode representing the JSON schema
     */
    public static JsonNode generateSchemaFromClass(Class<?> clazz) {
        try {
            JsonSchema schema = schemaGenerator.generateSchema(clazz);
            JsonNode schemaNode = mapper.valueToTree(schema);
            removeIdFields(schemaNode);
            return schemaNode;
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON schema", e);
        }
    }
    
    /**
     * Validate input parameters against a JSON schema
     *
     * @param schema The JSON schema to validate against
     * @param input The input parameters to validate
     * @return List of validation errors, empty list if valid
     */
    public static List<String> validateAgainstSchema(JsonNode schema, Map<String, Object> input) {
        List<String> errors = new ArrayList<>();
        
        try {
            // Convert input map to JsonNode
            JsonNode inputNode = mapper.valueToTree(input);
            
            // Create schema validator
            com.networknt.schema.JsonSchema validator = schemaFactory.getSchema(schema);
            
            // Validate
            Set<ValidationMessage> validationResult = validator.validate(inputNode);
            
            // Convert validation messages to strings
            if (!validationResult.isEmpty()) {
                for (ValidationMessage msg : validationResult) {
                    errors.add(msg.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error validating against schema", e);
            errors.add("Schema validation error: " + e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * Check if input parameters are valid against a JSON schema
     *
     * @param schema The JSON schema to validate against
     * @param input The input parameters to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(JsonNode schema, Map<String, Object> input) {
        return validateAgainstSchema(schema, input).isEmpty();
    }

    private static void removeIdFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.remove("id");

            // Process all child nodes
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                removeIdFields(fields.next().getValue());
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                removeIdFields(element);
            }
        }
    }
}