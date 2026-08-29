package com.uade.e_commerce.controller;

import java.util.List;

import com.uade.e_commerce.dto.UsuarioUpdateDTO;
import com.uade.e_commerce.service.ProductoService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import com.uade.e_commerce.dto.RegisterRequest;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.service.UsuarioService;

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
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/{id}")
    public Usuario getUsuarioById(@PathVariable Long id) {
        return usuarioService.getUsuarioById(id);
    }

    @PostMapping
    public UsuarioResponseDTO saveUsuario(@RequestBody RegisterRequest request) {
        return usuarioService.saveUsuario(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO updateUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateDTO dto) {
        return usuarioService.updateUsuario(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
    }
}
