package org.opengpa.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing category information including display name, description, and icon.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInfoDTO {
    private String name;
    private String displayName;
    private String description;
    private String icon;
}