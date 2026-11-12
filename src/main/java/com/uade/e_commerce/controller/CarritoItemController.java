package com.uade.e_commerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.CarritoItemRequestDTO;
import com.uade.e_commerce.dto.CarritoItemResponseDTO;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.security.AccesoUsuario;
import com.uade.e_commerce.service.CarritoItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


// Operaciones sobre un item puntual del carrito.
// Cada endpoint verifica que el item sea del carrito del usuario logueado (si no, 403):
// antes cualquier usuario autenticado podía ver, modificar o borrar items de otro.
@RestController
@RequestMapping("/api/carrito-items")
public class CarritoItemController {

    @Autowired
    private CarritoItemService carritoItemService;

    @GetMapping("/{itemId}")
    public ResponseEntity<CarritoItemResponseDTO> obtenerItem(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long itemId) {
        CarritoItem item = buscarItemPropio(usuarioLogueado, itemId);
        return ResponseEntity.ok(carritoItemService.convertirADTO(item));
    }

    @PostMapping("/{itemId}")
    public ResponseEntity<CarritoItemResponseDTO> actualizarCantidad(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long itemId,
            @RequestBody CarritoItemRequestDTO request) {
        buscarItemPropio(usuarioLogueado, itemId);
        CarritoItem item = carritoItemService.actualizarCantidad(itemId, request.getCantidad());
        return ResponseEntity.ok(carritoItemService.convertirADTO(item));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> eliminarItem(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long itemId) {
        buscarItemPropio(usuarioLogueado, itemId);
        carritoItemService.eliminarItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // Lista los items de todos los carritos: solo ADMIN (SecurityConfig).
    @GetMapping
    public ResponseEntity<List<CarritoItemResponseDTO>> obtenerTodos() {

        List<CarritoItem> items = carritoItemService.obtenerTodos();
        List<CarritoItemResponseDTO> response = items.stream()
                .map(carritoItemService::convertirADTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    // Busca el item (404 si no existe) y verifica que pertenezca al carrito del usuario logueado (403 si no).
    private CarritoItem buscarItemPropio(Usuario usuarioLogueado, Long itemId) {
        CarritoItem item = carritoItemService.obtenerPorId(itemId);
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, item.getCarrito().getUsuario().getId());
        return item;
    }
}
