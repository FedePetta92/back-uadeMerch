package com.uade.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.Producto;

/**
 * JPA Repository para la entidad Producto, proporciona métodos CRUD y consultas personalizadas a la DB, de la tabla Productos
 * Minimiza la cantidad de código bolier plate pq no debo hacer crud básico ProductoRepository
 */

/** Repositorio JPA para persistir productos y resolver busquedas del catalogo. */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findById(Long id);
    
}
