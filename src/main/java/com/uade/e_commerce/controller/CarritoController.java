package com.uade.e_commerce.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.CarritoItemRequestDTO;
import com.uade.e_commerce.dto.CarritoResponseDTO;
import com.uade.e_commerce.model.Carrito;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.security.AccesoUsuario;
import com.uade.e_commerce.service.CarritoItemService;
import com.uade.e_commerce.service.CarritoService;




@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    
    @Autowired
    private CarritoItemService carritoItemService;

    @GetMapping("/{usuarioId}")
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long usuarioId) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, usuarioId);
        // Si el usuario todavía no tiene carrito se crea vacío (antes devolvía 404)
        Carrito carrito = carritoService.obtenerOcrearCarritoPorUsuario(usuarioId);
        return ResponseEntity.ok(convertirADTO(carrito));
    }

    @PostMapping("/{usuarioId}/items")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long usuarioId,
            @RequestBody CarritoItemRequestDTO request) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, usuarioId);
        Carrito carrito = carritoService.agregarProducto(
                usuarioId, request.getProductoId(), request.getCantidad());
        return ResponseEntity.ok(convertirADTO(carrito));
    }

    @DeleteMapping("/{usuarioId}/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> eliminarProducto(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long usuarioId,
            @PathVariable Long itemId) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, usuarioId);
        Carrito carrito = carritoService.eliminarProducto(usuarioId, itemId);
        return ResponseEntity.ok(convertirADTO(carrito));
    }

    @GetMapping("/{usuarioId}/total")
    public ResponseEntity<BigDecimal> obtenerTotal(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long usuarioId) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, usuarioId);
        return ResponseEntity.ok(carritoService.obtenerTotal(usuarioId));
    }

    private CarritoResponseDTO convertirADTO(Carrito carrito) {
        List<com.uade.e_commerce.dto.CarritoItemResponseDTO> itemsDTO = carrito.getItems().stream()
                .map(carritoItemService::convertirADTO)
                .collect(Collectors.toList());

        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        dto.setUsuarioId(carrito.getUsuario().getId());
        dto.setItems(itemsDTO);
        dto.setTotal(carritoService.calcularTotal(carrito));
        return dto;
    }
}
