package com.uade.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.e_commerce.model.CarritoItem;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    Optional<CarritoItem> findById(Long id);

    // Quita un producto de todos los carritos (se usa al dar de baja el producto)
    @Modifying
    @Query("DELETE FROM CarritoItem ci WHERE ci.producto.id = :productoId")
    void deleteByProductoId(@Param("productoId") Long productoId);

}
