

package com.uade.e_commerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.OrdenCompraResponseDTO;
import com.uade.e_commerce.dto.PagoRequestDTO;
import com.uade.e_commerce.model.EstadoOrdenCompra;
import com.uade.e_commerce.model.OrdenCompra;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.security.AccesoUsuario;
import com.uade.e_commerce.service.OrdenCompraService;

@RestController
@RequestMapping("/api/ordenes-compra")

// Expone los 3 endpoints (checkout, historial por usuario y detalle de una orden).
// Devuelve el código HTTP según el resultado (201 si paga, 402 si rechaza).

public class OrdenCompraController {

    @Autowired
    private OrdenCompraService ordenService;

    // Compra todo el carrito del usuario. 201 si el pago se aprueba, 402 si se rechaza.
    @PostMapping("/{usuarioId}/checkout")
    public ResponseEntity<OrdenCompraResponseDTO> checkout(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long usuarioId,
            @RequestBody PagoRequestDTO request) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, usuarioId);
        OrdenCompra orden = ordenService.checkout(usuarioId, request.getNumeroTarjeta());
        HttpStatus status = orden.getEstado() == EstadoOrdenCompra.PAGADA
                ? HttpStatus.CREATED
                : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(ordenService.convertirADTO(orden));
    }

    // Historial de ordenes del usuario.
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<OrdenCompraResponseDTO>> listarPorUsuario(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long usuarioId) {
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, usuarioId);
        return ResponseEntity.ok(ordenService.listarPorUsuario(usuarioId));
    }

    // Detalle de una orden. Solo la puede ver el usuario que la hizo (403 si es de otro).
    @GetMapping("/{ordenId}")
    public ResponseEntity<OrdenCompraResponseDTO> obtenerOrden(
            @AuthenticationPrincipal Usuario usuarioLogueado,
            @PathVariable Long ordenId) {
        OrdenCompraResponseDTO orden = ordenService.obtenerPorId(ordenId);
        AccesoUsuario.validarMismoUsuario(usuarioLogueado, orden.getUsuarioId());
        return ResponseEntity.ok(orden);
    }
}