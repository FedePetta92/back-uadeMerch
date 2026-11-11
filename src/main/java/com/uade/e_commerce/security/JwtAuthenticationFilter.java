package com.uade.e_commerce.security;


import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.repository.UsuarioRepository;
import com.uade.e_commerce.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    // Se ejecuta en cada request: si trae un token JWT válido de un usuario activo, lo autentica.
    // Si no trae token, el token es inválido o el usuario está dado de baja, la request sigue sin autenticar
    // y Spring Security la rechaza en las rutas protegidas.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String authHeader = request.getHeader("Authorization");


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = authHeader.substring(7);

        try {


            String email = jwtService.extractEmail(token);


            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {


                // Si el usuario se dio de baja, su token deja de servir aunque no haya vencido
                Usuario usuario = usuarioRepository
                        .findByEmailAndActivoTrue(email)
                        .orElse(null);


                if (usuario != null && jwtService.isTokenValid(token)) {


                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    Collections.emptyList()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );


                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception e) {


            SecurityContextHolder.clearContext();
        }


        filterChain.doFilter(request, response);
    }


}
