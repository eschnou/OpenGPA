package org.opengpa.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileDTO {
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;
}