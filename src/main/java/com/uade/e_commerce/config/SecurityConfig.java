
package com.uade.e_commerce.config;

import com.uade.e_commerce.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Login público
                        .requestMatchers("/api/login/**")
                        .permitAll()

                        // Registro público: solamente genera USER
                        .requestMatchers(HttpMethod.POST, "/api/usuarios")
                        .permitAll()

                        // Consulta pública de productos
                        .requestMatchers(HttpMethod.GET, "/api/productos/**")
                        .permitAll()

                        // Gestión de productos: solo ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos",
                                "/api/productos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/productos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/productos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/productos/**"
                        )
                        .hasRole("ADMIN")

                        // Administración de usuarios
                        .requestMatchers(HttpMethod.GET, "/api/usuarios")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**")
                        .authenticated()

                        // Carritos: usuarios autenticados
                        .requestMatchers("/api/carritos/**")
                        .authenticated()

                        // El listado de items de TODOS los carritos es información de administración
                        .requestMatchers(HttpMethod.GET, "/api/carrito-items")
                        .hasRole("ADMIN")

                        // Un item puntual: el controller además verifica que sea del usuario logueado
                        .requestMatchers("/api/carrito-items/**")
                        .authenticated()

                        // Resto: requieren autenticación
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
