package org.opengpa.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.opengpa.server.dto.ChangePasswordDTO;
import org.opengpa.server.dto.UpdateUserProfileDTO;
import org.opengpa.server.dto.UserProfileDTO;
import org.opengpa.server.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Profile Management", description = "Endpoints for managing user profile and credentials")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(userDetails.getUsername()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserProfileDTO updateDto) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), updateDto));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordDTO passwordDto) {
        userService.changePassword(userDetails.getUsername(), passwordDto);
        return ResponseEntity.ok().build();
    }
}