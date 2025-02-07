package org.opengpa.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.server.config.ApplicationConfig;
import org.opengpa.server.dto.AuthRequest;
import org.opengpa.server.dto.AuthResponse;
import org.opengpa.server.dto.RegisterRequest;
import org.opengpa.server.dto.UserProfileDTO;
import org.opengpa.server.exceptions.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    public static final String LOGIN = "LOGIN";

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationConfig appConfig;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (appConfig.isClosedBeta()) {
            if (!appConfig.getInviteCodes().contains(request.getInviteCode())) {
                log.warn("User {} attempted to register with unknown code {}", request.getUsername(), request.getInviteCode());
                throw new BadRequestException("Invalid invite code");
            }

            log.info("User {} is signing up with invite code {}", request.getUsername(), request.getInviteCode());
        }

        UserProfileDTO userDto = userService.createUser(
                request.getUsername(),
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(
                userService.loadUserByUsername(request.getUsername())
        );

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        log.info("User {} is authenticated.", request.getUsername());

        var userDetails = userService.loadUserByUsername(request.getUsername());
        var userDto = userService.getUserByUsername(userDetails.getUsername());
        var token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }
}
