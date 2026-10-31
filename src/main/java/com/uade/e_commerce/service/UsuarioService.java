package com.uade.e_commerce.service;

import java.time.LocalDateTime;
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
import com.uade.e_commerce.repository.CarritoRepository;
import com.uade.e_commerce.repository.UsuarioRepository;
import com.uade.e_commerce.security.CodificadorPassword;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorPassword passwordEncoder;
    private final CarritoRepository carritoRepository;

    @Autowired
    private JwtService jwtService;

    UsuarioService(UsuarioRepository usuarioRepository, CodificadorPassword passwordEncoder, CarritoRepository carritoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.carritoRepository = carritoRepository;
    }

    // Devuelve todos los usuarios activos (los dados de baja no se listan).
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findByActivoTrue();
    }

    // Busca un usuario activo por id. Si no existe o está dado de baja, lanza RecursoNoEncontradoException (404).
    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findByIdAndActivoTrue(id).orElseThrow(() -> new RecursoNoEncontradoException("Id no encontrado"));
    }

    // Registra un usuario nuevo: valida que el email no esté usado, encripta la contraseña y lo guarda activo.
    // Devuelve sus datos sin la contraseña.
    // El email se busca también entre los dados de baja, porque en la tabla tiene que ser único.
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
        response.setEmail(saved.getEmail());
        response.setFechaNacimiento(saved.getFechaNacimiento());
        response.setSexo(saved.getSexo());
        return response;
    }

    // Actualiza el nombre y el apellido de un usuario activo y devuelve sus datos.
    // Si no existe o está dado de baja, lanza RecursoNoEncontradoException (404).
    public UsuarioResponseDTO updateUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario user = usuarioRepository.findByIdAndActivoTrue(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        user.setNombre(dto.getNombre());
        user.setApellido(dto.getApellido());
        Usuario updated = usuarioRepository.save(user);
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(updated.getId());
        response.setNombre(updated.getNombre());
        response.setApellido(updated.getApellido());
        response.setEmail(updated.getEmail());
        return response;
    }

    // Da de baja a un usuario con borrado lógico: lo marca como inactivo y guarda la fecha de baja.
    // No se elimina la fila porque sus órdenes de compra lo siguen referenciando (historial).
    // El carrito sí se borra: es un dato temporal, no historial. Al borrarlo, la cascada elimina sus items.
    // Si no existe o ya estaba dado de baja, lanza RecursoNoEncontradoException (404).
    public void deleteUsuario(Long id) {
        Usuario usuario = usuarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        carritoRepository.findByUsuarioId(id).ifPresent(carritoRepository::delete);

        usuario.setActivo(false);
        usuario.setFechaBaja(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = getUsuarioById(id);
        String passwordEncriptada = passwordEncoder.encode(nuevaPassword);
        usuario.setPassword(passwordEncriptada);
        usuarioRepository.save(usuario);
    }


    // Valida email y contraseña y, si son correctos, devuelve un token JWT y el id del usuario
    // (el front necesita el id para llamar a los endpoints del carrito).
    // Si el usuario no existe, está dado de baja o la contraseña es incorrecta, lanza UnauthorizedException.
    public LoginResponseDTO login(LoginRequest request) {

        // Un usuario dado de baja no puede iniciar sesión
        Usuario usuario = usuarioRepository
                .findByEmailAndActivoTrue(request.getEmail())
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
}