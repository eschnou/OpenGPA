package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pagination {

  @JsonProperty("total_items")
  private Integer totalItems;

  @JsonProperty("total_pages")
  private Integer totalPages;

  @JsonProperty("current_page")
  private Integer currentPage;

  @JsonProperty("page_size")
  private Integer pageSize;
}

