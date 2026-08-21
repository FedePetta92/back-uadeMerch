package com.uade.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import com.uade.e_commerce.model.Producto;

/**
 * JPA Repository para la entidad Producto, proporciona métodos CRUD
 *  y consultas personalizadas a la DB, de la tabla Productos
 * Minimiza la cantidad de código bolier plate pq no debo hacer crud básico
 * ProductoRepository
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //jpa save, findAll, findById, deleteById, etc.. ya vienen implementados por JpaRepository

    // Query Method genera un sql en base al nombre del método
    //"SELECT p FROM Producto p WHERE p.precio < :precio
    List<Producto> findByPrecioLessThan(Double precio);

    List<Producto> findByPrecioBetween(Double precioMin, Double precioMax);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    

}
