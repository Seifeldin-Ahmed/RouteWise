package com.fawry.routing.config.security;

import com.fawry.routing.config.security.filters.JsonLoginFilterConfig;
import com.fawry.routing.config.security.filters.JwtAuthFilter;
import com.fawry.routing.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security.
 *
 * <p>URL rules are deliberately coarse - login and signup are open, everything else needs a
 * valid token. The ADMIN/USER split is expressed per endpoint with {@code @PreAuthorize},
 * and ownership of biller data is enforced again inside the service layer, because a role
 * check alone would let one biller read another biller data by editing the URL.</p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService,
                                                            BCryptPasswordEncoder bcryptPasswordEncoder) {
        var auth = new DaoAuthenticationProvider(userService);
        auth.setPasswordEncoder(bcryptPasswordEncoder);
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager,
                                           DaoAuthenticationProvider authenticationProvider) throws Exception {
        var loginFilterConfig = new JsonLoginFilterConfig(authManager);

        http.authorizeHttpRequests(configurer -> configurer
                        .requestMatchers("/login", "/signup").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> {
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(loginFilterConfig, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(configurer -> configurer
                        // Authenticated, but lacking the role for this resource.
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"status\":\"fail\",\"message\":\"Access Denied\"}");
                        })
                        // Not authenticated at all.
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"status\":\"fail\",\"message\":\"Unauthorized\"}");
                        })
                );

        return http.build();
    }
}
