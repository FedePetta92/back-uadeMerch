package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.*;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.exceptions.UnauthorizedException;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.repository.UsuarioRepository;
import com.uade.e_commerce.security.CodificadorPassword;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorPassword passwordEncoder;

    @Autowired
    private JwtService jwtService;

    UsuarioService(UsuarioRepository usuarioRepository, CodificadorPassword passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Id no encontrado"));
    }

    public UsuarioResponseDTO saveUsuario(RegisterUsuarioRequest request) {
        String passwordEncriptada =
                passwordEncoder.encode(request.getPassword());
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncriptada)
                .build();
        Usuario saved = usuarioRepository.save(usuario);
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(saved.getId());
        response.setNombre(saved.getNombre());
        response.setApellido(saved.getApellido());
        return response;
    }

    public UsuarioResponseDTO updateUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario user = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        user.setNombre(dto.getNombre());
        user.setApellido(dto.getApellido());
        Usuario updated = usuarioRepository.save(user);
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(updated.getId());
        response.setNombre(updated.getNombre());
        response.setApellido(updated.getApellido());
        return response;
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = getUsuarioById(id);
        String passwordEncriptada = passwordEncoder.encode(nuevaPassword);
        usuario.setPassword(passwordEncriptada);
        usuarioRepository.save(usuario);
    }


    public LoginResponseDTO login(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UnauthorizedException("Usuario inexistente")
                );

        boolean passwordValida = passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword()
        );

        if (!passwordValida) {
            throw new RuntimeException("Contraseña incorrecta");
        }


        String token = jwtService.generateToken(usuario);

        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }
}
