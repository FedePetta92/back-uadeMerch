package com.uade.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
