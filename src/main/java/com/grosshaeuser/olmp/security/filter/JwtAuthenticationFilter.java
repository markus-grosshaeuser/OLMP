package com.grosshaeuser.olmp.security.filter;

import com.grosshaeuser.olmp.security.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());

        try {
            Claims claims = jwtService.parseAndValidate(token);

            List<?> rawAuthorities = claims.get("authorities", List.class);
            List<SimpleGrantedAuthority> authorities = rawAuthorities == null
                    ? List.of()
                    : rawAuthorities.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            Number accountId = claims.get("accountId", Number.class);
            Number memberId = claims.get("memberId", Number.class);

            if (accountId == null || memberId == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT claims");
                return;
            }

            MemberJwtPrincipal principal = new MemberJwtPrincipal(
                    claims.getSubject(),
                    accountId.longValue(),
                    memberId.longValue()
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    token,
                    authorities
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        } catch (RuntimeException exception) {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public record MemberJwtPrincipal(
            String email,
            Long accountId,
            Long memberId
    ) {
    }
}