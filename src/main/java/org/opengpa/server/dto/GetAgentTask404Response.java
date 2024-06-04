package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * GetAgentTask404Response
 */

@JsonTypeName("getAgentTask_404_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-05-22T20:04:39.683430+02:00[Europe/Brussels]", comments = "Generator version: 7.4.0")
public class GetAgentTask404Response {

  private String message;

  public GetAgentTask404Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GetAgentTask404Response(String message) {
    this.message = message;
  }

  public GetAgentTask404Response message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Message stating the entity was not found
   * @return message
  */
  @NotNull 
  @Schema(name = "message", example = "Unable to find entity with the provided id", description = "Message stating the entity was not found", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetAgentTask404Response getAgentTask404Response = (GetAgentTask404Response) o;
    return Objects.equals(this.message, getAgentTask404Response.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetAgentTask404Response {\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

