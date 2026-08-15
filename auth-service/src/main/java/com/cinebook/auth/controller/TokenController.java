package com.cinebook.auth.controller;

import com.cinebook.auth.dto.ApiResponse;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class TokenController {

    private final JwtUtil jwtUtil;

    public TokenController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /** Exchanges a valid (non-expired) refresh token for a new access token, without re-authenticating. */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            throw new ApiException("refreshToken is required.", HttpStatus.BAD_REQUEST);
        }
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(refreshToken);
        } catch (Exception e) {
            throw new ApiException("Refresh token invalid or expired. Please log in again.", HttpStatus.UNAUTHORIZED);
        }
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new ApiException("Not a refresh token.", HttpStatus.BAD_REQUEST);
        }
        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);

        String newAccess = jwtUtil.generateAccessToken(userId, email, name, role);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed.", Map.of("accessToken", newAccess)));
    }
}
