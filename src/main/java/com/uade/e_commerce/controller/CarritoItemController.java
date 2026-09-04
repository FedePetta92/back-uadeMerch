package com.uade.e_commerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.CarritoItemRequestDTO;
import com.uade.e_commerce.dto.CarritoItemResponseDTO;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.repository.CarritoItemRepository;
import com.uade.e_commerce.service.CarritoItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/carrito-items")
public class CarritoItemController {

    @Autowired
    private CarritoItemService carritoItemService;

    @GetMapping("/{itemId}")
    public ResponseEntity<CarritoItemResponseDTO> obtenerItem(@PathVariable Long itemId) {
        CarritoItem item = carritoItemService.obtenerPorId(itemId);
        return ResponseEntity.ok(carritoItemService.convertirADTO(item));
    }

    @PostMapping("/{itemId}")
    public ResponseEntity<CarritoItemResponseDTO> actualizarCantidad(
            @PathVariable Long itemId,
            @RequestBody CarritoItemRequestDTO request) {
        CarritoItem item = carritoItemService.actualizarCantidad(itemId, request.getCantidad());
        return ResponseEntity.ok(carritoItemService.convertirADTO(item));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long itemId) {
        carritoItemService.eliminarItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
