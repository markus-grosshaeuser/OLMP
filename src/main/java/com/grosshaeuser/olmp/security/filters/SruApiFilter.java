package com.grosshaeuser.olmp.security.filters;

import com.grosshaeuser.olmp.security.OlmpUserDetailsService;
import com.grosshaeuser.olmp.security.entities.User;
import com.grosshaeuser.olmp.security.repositories.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SruApiFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final OlmpUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String apiKey = extractApiKey(request);
            String[] apiKeyParts = isolateApiKeyParts(apiKey);
            String apiKeyUUID = apiKeyParts[0];
            String apiKeySecret = apiKeyParts[1];
            UUID uuid = UUID.fromString(apiKeyUUID);
            attemptToSetAuthentication(request, uuid, apiKeySecret);
        } catch (IllegalArgumentException _) {

        }
        filterChain.doFilter(request, response);
    }

    private String extractApiKey(HttpServletRequest request) throws IllegalArgumentException{
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("X-API-KEY: ")) {
            throw new IllegalArgumentException("Invalid API key format");
        }
        return authorizationHeader.substring(11);
    }

    private String[] isolateApiKeyParts(String apiKey) throws IllegalArgumentException{
        String[] apiKeyParts = apiKey.split("\\.");
        if (apiKeyParts.length != 2) {
            throw new IllegalArgumentException("Invalid API key format");
        }
        String apiKeyUUID = apiKeyParts[0];
        String apiKeySecret = apiKeyParts[1];

        if (!StringUtils.hasText(apiKeyUUID) || !StringUtils.hasText(apiKeySecret)) {
            throw new IllegalArgumentException("Invalid API key format");
        }
        return apiKeyParts;
    }

    private void attemptToSetAuthentication(HttpServletRequest request, UUID uuid, String apiKeySecret) {
        apiKeyRepository.findByIdWithFullUserContext(uuid)
                .ifPresent(key -> {
                    if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(OffsetDateTime.now())) {
                        return;
                    }

                    if (passwordEncoder.matches(apiKeySecret, key.getSecret())) {
                        User user = key.getUser();
                        if (user == null || !user.isEnabled() || user.isLocked()) {
                            return;
                        }
                        UserDetails userDetails = this.userDetailsService.fromUserInstance(user);
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                });
    }
}
