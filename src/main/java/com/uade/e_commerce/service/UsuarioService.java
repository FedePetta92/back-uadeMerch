package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.LoginRequest;
import com.uade.e_commerce.dto.LoginResponseDTO;
import com.uade.e_commerce.dto.RegisterUsuarioRequest;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.dto.UsuarioUpdateDTO;
import com.uade.e_commerce.exceptions.EmailDuplicadoException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.exceptions.UnauthorizedException;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.repository.UsuarioRepository;
import com.uade.e_commerce.security.CodificadorPassword;

import jakarta.transaction.Transactional;

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

    public List<UsuarioResponseDTO> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO getUsuarioById(Long id) {
        return toResponseDTO(buscarUsuario(id));
    }

    public UsuarioResponseDTO saveUsuario(RegisterUsuarioRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con ese email");
        }

        String passwordEncriptada =
                passwordEncoder.encode(request.getPassword());
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncriptada)
                .fechaNacimiento(request.getFechaNacimiento())
                .sexo(request.getSexo())
                .build();
        Usuario saved = usuarioRepository.save(usuario);
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(saved.getId());
        response.setNombre(saved.getNombre());
        response.setApellido(saved.getApellido());
        response.setFechaNacimiento(saved.getFechaNacimiento());
        response.setSexo(saved.getSexo());
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
        Usuario usuario = buscarUsuario(id);
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
            throw new UnauthorizedException("Contraseña incorrecta");
        }


        String token = jwtService.generateToken(usuario);

        return LoginResponseDTO.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .build();
    }

    // Uso interno: devuelve la entidad completa (incluye el hash de la contraseña).
    // Nunca debe salir tal cual por la API.
    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Id no encontrado"));
    }

    // Convierte la entidad en el DTO público: sin password.
    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setFechaNacimiento(usuario.getFechaNacimiento());
        dto.setSexo(usuario.getSexo());
        return dto;
    }
}