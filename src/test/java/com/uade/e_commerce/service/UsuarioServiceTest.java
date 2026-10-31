package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.LoginRequest;
import com.uade.e_commerce.dto.LoginResponseDTO;
import com.uade.e_commerce.dto.RegisterUsuarioRequest;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.dto.UsuarioUpdateDTO;
import com.uade.e_commerce.exceptions.EmailDuplicadoException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.exceptions.UnauthorizedException;
import com.uade.e_commerce.model.Carrito;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.model.UsuarioSexo;
import com.uade.e_commerce.repository.CarritoRepository;
import com.uade.e_commerce.repository.UsuarioRepository;
import com.uade.e_commerce.security.CodificadorPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodificadorPassword passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(usuarioService, "jwtService", jwtService);
    }

    // nombre de los test -> metodo_escenario_resultadoEsperado

    @Test
    void saveUsuario_emailNuevo_guardaYDevuelveDTO() {
        RegisterUsuarioRequest request = RegisterUsuarioRequest.builder()
                .nombre("Mauro")
                .apellido("Aguilera")
                .email("mauro@test.com")
                .password("1234")
                .fechaNacimiento(LocalDate.of(1998, 1, 1))
                .sexo(UsuarioSexo.MASCULINO)
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .id(1L)
                .nombre("Mauro")
                .apellido("Aguilera")
                .email("mauro@test.com")
                .password("hash-falso")
                .fechaNacimiento(LocalDate.of(1998, 1, 1))
                .sexo(UsuarioSexo.MASCULINO)
                .build();

        when(usuarioRepository.findByEmail("mauro@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234")).thenReturn("hash-falso");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        UsuarioResponseDTO response = usuarioService.saveUsuario(request);

        assertThat(response.getNombre()).isEqualTo("Mauro");
        assertThat(response.getApellido()).isEqualTo("Aguilera");
        assertThat(response.getEmail()).isEqualTo("mauro@test.com");
        assertThat(response.getId()).isEqualTo(1L);

        // El usuario nuevo se guarda activo
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().isActivo()).isTrue();
    }

    @Test
    void saveUsuario_emailDuplicado_lanzaExcepcion() {
        RegisterUsuarioRequest request = RegisterUsuarioRequest.builder()
                .nombre("Mauro")
                .apellido("Aguilera")
                .email("duplicado@test.com")
                .password("1234")
                .build();

        Usuario usuarioExistente = Usuario.builder().id(1L).email("duplicado@test.com").build();

        when(usuarioRepository.findByEmail("duplicado@test.com")).thenReturn(Optional.of(usuarioExistente));

        assertThatThrownBy(() -> usuarioService.saveUsuario(request))
                .isInstanceOf(EmailDuplicadoException.class)
                .hasMessage("Ya existe un usuario registrado con ese email");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_credencialesValidas_devuelveToken() {
        LoginRequest request = LoginRequest.builder()
                .email("mauro@test.com")
                .password("1234")
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("mauro@test.com")
                .password("hash-almacenado")
                .build();

        when(usuarioRepository.findByEmailAndActivoTrue("mauro@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "hash-almacenado")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("token-falso");

        LoginResponseDTO response = usuarioService.login(request);

        assertThat(response.getToken()).isEqualTo("token-falso");
        assertThat(response.getUsuarioId()).isEqualTo(1L);
    }

    @Test
    void login_usuarioInexistente_lanzaExcepcion() {
        LoginRequest request = LoginRequest.builder()
                .email("noexiste@test.com")
                .password("1234")
                .build();

        when(usuarioRepository.findByEmailAndActivoTrue("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario inexistente");
    }

    @Test
    void login_passwordIncorrecta_lanzaExcepcion() {
        LoginRequest request = LoginRequest.builder()
                .email("mauro@test.com")
                .password("mal")
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("mauro@test.com")
                .password("hash-almacenado")
                .build();

        when(usuarioRepository.findByEmailAndActivoTrue("mauro@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mal", "hash-almacenado")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Contraseña incorrecta");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void updateUsuario_existente_actualizaDatosYDevuelveDTO() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("NuevoNombre");
        dto.setApellido("NuevoApellido");

        Usuario usuarioExistente = Usuario.builder()
                .id(1L)
                .nombre("ViEjoNombre")
                .apellido("ViejoApellido")
                .build();

        Usuario usuarioActualizado = Usuario.builder()
                .id(1L)
                .nombre("NuevoNombre")
                .apellido("NuevoApellido")
                .build();

        when(usuarioRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(usuarioExistente)).thenReturn(usuarioActualizado);

        UsuarioResponseDTO response = usuarioService.updateUsuario(1L, dto);

        assertThat(response.getNombre()).isEqualTo("NuevoNombre");
        assertThat(response.getApellido()).isEqualTo("NuevoApellido");
    }

    @Test
    void updateUsuario_noExistente_lanzaExcepcion() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Nombre");
        dto.setApellido("Apellido");

        when(usuarioRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.updateUsuario(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void login_usuarioDadoDeBaja_lanzaExcepcion() {
        LoginRequest request = LoginRequest.builder()
                .email("baja@test.com")
                .password("1234")
                .build();

        // findByEmailAndActivoTrue no encuentra a los usuarios dados de baja
        when(usuarioRepository.findByEmailAndActivoTrue("baja@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario inexistente");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void deleteUsuario_conCarrito_borraElCarritoYDaDeBajaAlUsuario() {
        Usuario usuario = Usuario.builder().id(1L).email("mauro@test.com").build();
        Carrito carrito = Carrito.builder().id(10L).usuario(usuario).build();

        when(usuarioRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        usuarioService.deleteUsuario(1L);

        verify(carritoRepository).delete(carrito);
        assertThat(usuario.isActivo()).isFalse();
        assertThat(usuario.getFechaBaja()).isNotNull();
        verify(usuarioRepository).save(usuario);
        // Borrado lógico: nunca se elimina la fila
        verify(usuarioRepository, never()).deleteById(any());
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void deleteUsuario_sinCarrito_soloDaDeBajaAlUsuario() {
        Usuario usuario = Usuario.builder().id(1L).email("mauro@test.com").build();

        when(usuarioRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        usuarioService.deleteUsuario(1L);

        verify(carritoRepository, never()).delete(any());
        assertThat(usuario.isActivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deleteUsuario_noExistenteODadoDeBaja_lanzaExcepcion() {
        when(usuarioRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.deleteUsuario(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");

        verify(usuarioRepository, never()).save(any());
    }
}
