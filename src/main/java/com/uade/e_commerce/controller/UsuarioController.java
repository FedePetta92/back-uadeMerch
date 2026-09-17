package com.uade.e_commerce.controller;

import java.util.List;

import com.uade.e_commerce.dto.UsuarioUpdateDTO;
import com.uade.e_commerce.service.ProductoService;
//import lombok.Data;
import org.springframework.web.bind.annotation.*;

import com.uade.e_commerce.dto.RegisterUsuarioRequest;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
/** Expone los endpoints REST para consultar y actualizar usuarios autenticados. */
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ProductoService productoService;

    UsuarioController(UsuarioService usuarioService, ProductoService productoService) {
        this.usuarioService = usuarioService;
        this.productoService = productoService;
    }

    /** Consulta datos mediante el endpoint REST. */

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    /** Consulta datos mediante el endpoint REST. */

    @GetMapping("/{id}")
    public Usuario getUsuarioById(@PathVariable Long id) {
        return usuarioService.getUsuarioById(id);
    }

    /** Procesa la operacion solicitada mediante el endpoint REST. */

    @PostMapping
    public UsuarioResponseDTO saveUsuario(@RequestBody RegisterUsuarioRequest request) {
        return usuarioService.saveUsuario(request);
    }

    /** Actualiza datos mediante el endpoint REST. */

    @PutMapping("/{id}")
    public UsuarioResponseDTO updateUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateDTO dto) {
        return usuarioService.updateUsuario(id, dto);
    }

    /** Elimina datos mediante el endpoint REST. */

    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
    }
}
