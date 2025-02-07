package org.opengpa.server.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengpa.server.service.JwtService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/v1/auth/register",
            "/api/v1/auth/login"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (shouldNotFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No Bearer token found in request headers");
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            final String username;

            try {
                username = jwtService.extractUsername(jwt);
            } catch (SignatureException e) {
                log.error("Invalid JWT signature: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
                return;
            } catch (MalformedJwtException e) {
                log.error("Malformed JWT token: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Malformed JWT token");
                return;
            } catch (ExpiredJwtException e) {
                log.error("Expired JWT token: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Expired JWT token");
                return;
            } catch (Exception e) {
                log.error("JWT token validation failed: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT token validation failed");
                return;
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                try {
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        MDC.put("username", username);
                        log.debug("Successfully authenticated user: {}", username);
                    } else {
                        log.warn("Token validation failed for user: {}", username);
                        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token");
                        return;
                    }
                } catch (Exception e) {
                    log.error("Token validation error for user {}: {}", username, e.getMessage());
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token validation error");
                    return;
                }
            }

            // Add security headers
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("username");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
        response.getWriter().flush();
    }
}