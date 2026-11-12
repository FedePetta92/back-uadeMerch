package com.uade.e_commerce.controller;

import com.uade.e_commerce.dto.RegisterUsuarioRequest;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.dto.UsuarioUpdateDTO;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.security.AccesoUsuario;
import com.uade.e_commerce.service.ProductoService;
import com.uade.e_commerce.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//import lombok.Data;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ProductoService productoService;

    UsuarioController(UsuarioService usuarioService, ProductoService productoService) {
        this.usuarioService = usuarioService;
        this.productoService = productoService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO getUsuarioById(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long id) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, id);
        return usuarioService.getUsuarioById(id);
    }

    // Registro público. @Valid aplica las validaciones de RegisterUsuarioRequest (400 si algún dato es inválido).
    @PostMapping
    public UsuarioResponseDTO saveUsuario(@Valid @RequestBody RegisterUsuarioRequest request) {
        return usuarioService.saveUsuario(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO updateUsuario(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long id,
            @RequestBody UsuarioUpdateDTO dto) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, id);
        return usuarioService.updateUsuario(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteUsuario(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long id) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, id);
        usuarioService.deleteUsuario(id);
    }
}
