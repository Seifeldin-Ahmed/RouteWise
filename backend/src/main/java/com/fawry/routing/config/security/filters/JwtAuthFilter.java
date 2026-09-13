package com.fawry.routing.config.security.filters;

import com.fawry.routing.config.security.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        var jwtUtils = new JwtUtils();
        if (header != null && header.startsWith("Bearer ")) {
            var token = header.substring(7);
            try {
                // throws exceptions if token invalid/expired
                jwtUtils.validateToken(token);

                var userId = jwtUtils.getId(token);
                var roles = jwtUtils.getRoles(token);
                if (roles == null) {
                    roles = List.of();
                }

                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                // The user id is the principal, so it travels with the request on its own.
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, authorities));

            } catch (ExpiredJwtException e) {
                writeFailure(response, "Token expired");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                // covers SecurityException, MalformedJwtException, UnsupportedJwtException
                writeFailure(response, "Invalid token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeFailure(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":\"fail\",\"message\":\"" + message + "\"}");
    }
}
