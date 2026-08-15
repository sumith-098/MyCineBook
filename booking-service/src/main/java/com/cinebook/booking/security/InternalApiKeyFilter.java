package com.cinebook.booking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Guards /api/bookings/internal/** — these are service-to-service calls (e.g. catalog-service
 * checking active-booking counts) that carry NO end-user JWT at all. A valid X-Internal-Api-Key
 * header grants a synthetic ROLE_INTERNAL_SERVICE authority instead.
 */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String expectedKey;

    public InternalApiKeyFilter(@Value("${app.internal.api-key}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/bookings/internal/")) {
            String provided = request.getHeader("X-Internal-Api-Key");
            if (provided != null && !provided.isBlank() && provided.equals(expectedKey)) {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"));
                var authToken = new UsernamePasswordAuthenticationToken("internal-service", null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
