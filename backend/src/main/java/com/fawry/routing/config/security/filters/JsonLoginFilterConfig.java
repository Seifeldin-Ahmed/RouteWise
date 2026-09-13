package com.fawry.routing.config.security.filters;

import com.fawry.routing.config.security.utils.JwtUtils;
import com.fawry.routing.config.security.utils.MyUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonLoginFilterConfig extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonLoginFilterConfig(AuthenticationManager authManager) {
        this.setAuthenticationManager(authManager);
        this.setFilterProcessesUrl("/login");

        // success handler
        this.setAuthenticationSuccessHandler((request, response, auth) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            var userDetails = (MyUserDetails) auth.getPrincipal();
            var roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .toList();

            var token = JwtUtils.generateToken(userDetails.getUsername(), userDetails.getId(), roles);
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Login successful\""
                    + ",\"token\":\"" + token + "\""
                    + ",\"userId\":" + userDetails.getId()
                    + ",\"email\":\"" + userDetails.getUsername() + "\""
                    + ",\"role\":\"" + roles.getFirst() + "\"}");
        });

        // failure handler
        this.setAuthenticationFailureHandler((request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":\"fail\",\"message\":\"Invalid email or password\"}");
        });
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            var creds = objectMapper.readValue(request.getInputStream(), Map.class);

            var token =
                    new UsernamePasswordAuthenticationToken(creds.get("email"), creds.get("password"));

            return this.getAuthenticationManager().authenticate(token);

        } catch (AuthenticationException e) {
            throw e; // let Spring handle it
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
