package com.uade.e_commerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.CarritoItemRequestDTO;
import com.uade.e_commerce.dto.CarritoResponseDTO;
import com.uade.e_commerce.model.Carrito;
import com.uade.e_commerce.service.CarritoService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/{usuarioId}")
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.obtenerCarritoPorUsuario(usuarioId);
        return ResponseEntity.ok(convertirADTO(carrito));
    }

    @PostMapping("/{usuarioId}/items")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(
            @PathVariable Long usuarioId,
            @RequestBody CarritoItemRequestDTO request) {
        Carrito carrito = carritoService.agregarProducto(
                usuarioId, request.getProductoId(), request.getCantidad());
        return ResponseEntity.ok(convertirADTO(carrito));
    }

    @DeleteMapping("/{usuarioId}/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> eliminarProducto(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId) {
        Carrito carrito = carritoService.eliminarProducto(usuarioId, itemId);
        return ResponseEntity.ok(convertirADTO(carrito));
    }

    @GetMapping("/{usuarioId}/total")
    public ResponseEntity<Double> obtenerTotal(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carritoService.obtenerTotal(usuarioId));
    }

    private CarritoResponseDTO convertirADTO(Carrito carrito) {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        dto.setItems(carrito.getItems());
        dto.setTotal(carritoService.calcularTotal(carrito));
        return dto;
    }
}
