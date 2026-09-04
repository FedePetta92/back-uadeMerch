package com.uade.e_commerce.service;

import java.util.List;

import com.uade.e_commerce.dto.UsuarioUpdateDTO;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;

import org.springframework.stereotype.Service;
import com.uade.e_commerce.dto.RegisterUsuarioRequest;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Id no encontrado"));
    }

    public UsuarioResponseDTO saveUsuario(RegisterUsuarioRequest request) {
        Usuario usuario = new Usuario(null, request.getNombre(), request.getApellido(), request.getEmail(), request.getPassword());
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
}
