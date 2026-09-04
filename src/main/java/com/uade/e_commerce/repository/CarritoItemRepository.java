package com.uade.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.CarritoItem;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    Optional<CarritoItem> findById(Long id);

}
