
package com.fundoonotes.security;

import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtil tokenUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            TokenUtil tokenUtil,
            UserRepository userRepository
    ) {
        this.tokenUtil = tokenUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        // If no token is supplied, leave authorization to Spring Security.
        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid Authorization header"
            );
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            // Reject tokens with invalid signatures or expiration.
            Long userId = tokenUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "User no longer exists"
                );
                return;
            }

            // Mark the verified user as authenticated for this request.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getId(),
                            null,
                            List.of()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException exception) {

            SecurityContextHolder.clearContext();

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
