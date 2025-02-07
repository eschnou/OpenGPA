package org.opengpa.server.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opengpa.core.workspace.Document;

import java.time.ZonedDateTime;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserInputStreamEventDTO.class, name = "userInput"),
        @JsonSubTypes.Type(value = SystemOuputStreamEventDTO.class, name = "systemOutput"),
        @JsonSubTypes.Type(value = ActionStreamEventDTO.class, name = "systemAction")
})
@Getter
@AllArgsConstructor
public class AbstractStreamEventDTO {
    private String owner;
    private String summary;
    private List<Document> documents;
    private ZonedDateTime timestamp;
}
