package org.opengpa.server.service;

import lombok.RequiredArgsConstructor;
import org.opengpa.server.config.ApplicationConfig;
import org.opengpa.server.dto.ChangePasswordDTO;
import org.opengpa.server.dto.UpdateUserProfileDTO;
import org.opengpa.server.dto.UserProfileDTO;
import org.opengpa.server.exceptions.AccessDeniedException;
import org.opengpa.server.exceptions.ResourceNotFoundException;
import org.opengpa.server.exceptions.UserAlreadyExistsException;
import org.opengpa.server.model.User;
import org.opengpa.server.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final String REGISTER = "REGISTER";
    private static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationConfig appConfig;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    // Internal API - Returns entities for service-to-service communication
    public User getInternalUserByUsername(String username) {
        return userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Transactional
    public UserProfileDTO createUser(String username, String name, String email, String password) {
        // Normalize to avoid duplicate with space/uppercase
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        String normalizedName = normalizeName(name);

        // Check if username or email already exists
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        var user = User.builder()
                .username(normalizedUsername)
                .name(normalizedName)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(password))
                .build();

        // Save the user
        user = userRepository.save(user);

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserByUsername(String username) {
        return userRepository.findByUsername(normalizeUsername(username))
                .map(this::mapToDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile(String username) {
        User user = getInternalUserByUsername(normalizeUsername(username));
        return mapToDTO(user);
    }

    @Transactional
    public UserProfileDTO updateProfile(String username, UpdateUserProfileDTO updateDto) {
        String normalizedUsername = normalizeUsername(username);
        User user = getInternalUserByUsername(normalizedUsername);

        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists");
            }
            user.setEmail(updateDto.getEmail());
        }

        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }

        user = userRepository.save(user);
        return getCurrentUserProfile(normalizedUsername);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordDTO passwordDto) {
        User user = getInternalUserByUsername(username);

        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new AccessDeniedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }

    private UserProfileDTO mapToDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public String normalizeUsername(String username) {
        if (username == null) return null;
        return username.toLowerCase().trim();
    }

    public String normalizeEmail(String email) {
        if (email == null) return null;
        return email.toLowerCase().trim();
    }

    public String normalizeName(String name) {
        if (name == null) return null;
        return name.trim();
    }
}
