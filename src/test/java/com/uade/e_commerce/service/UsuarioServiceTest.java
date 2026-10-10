package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.LoginRequest;
import com.uade.e_commerce.dto.LoginResponseDTO;
import com.uade.e_commerce.dto.RegisterUsuarioRequest;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.dto.UsuarioUpdateDTO;
import com.uade.e_commerce.exceptions.EmailDuplicadoException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.exceptions.UnauthorizedException;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.model.UsuarioSexo;
import com.uade.e_commerce.repository.UsuarioRepository;
import com.uade.e_commerce.security.CodificadorPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        assertThat(response.getId()).isEqualTo(1L);
        verify(usuarioRepository).save(any(Usuario.class));
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

        when(usuarioRepository.findByEmail("mauro@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "hash-almacenado")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("token-falso");

        LoginResponseDTO response = usuarioService.login(request);

        assertThat(response.getToken()).isEqualTo("token-falso");
    }

    @Test
    void login_usuarioInexistente_lanzaExcepcion() {
        LoginRequest request = LoginRequest.builder()
                .email("noexiste@test.com")
                .password("1234")
                .build();

        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

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

        when(usuarioRepository.findByEmail("mauro@test.com")).thenReturn(Optional.of(usuario));
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

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
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

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.updateUsuario(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");
    }
}
