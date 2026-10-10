package com.uade.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.OrdenCompra;

// Acceso a la tabla de ordenes, guarda y busca por id. Tiene una consulta para traer las ordenes de un usuario de la más nueva a la mas vieja.

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}