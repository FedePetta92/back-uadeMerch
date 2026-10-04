package com.uade.e_commerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce.dto.OrdenCompraItemResponseDTO;
import com.uade.e_commerce.dto.OrdenCompraResponseDTO;
import com.uade.e_commerce.exceptions.OrdenCompraInvalidaException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Carrito;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.model.EstadoOrdenCompra;
import com.uade.e_commerce.model.OrdenCompra;
import com.uade.e_commerce.model.OrdenCompraItem;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.OrdenCompraRepository;
import com.uade.e_commerce.repository.ProductoRepository;
import com.uade.e_commerce.service.PagoSimuladoService.ResultadoPago;

@Service

// Toma el carrito, valida stock y cantidades, calcula el total, llama al pago simulado y, si se aprueba, descuenta el stock y vacía el carrito. 
// También arma las respuestas para el historial y el detalle.

public class OrdenCompraService {

    @Autowired
    private OrdenCompraRepository ordenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private PagoSimuladoService pagoSimuladoService;

    //Convierte el carrito del usuario en una orden y la paga con el metodo simulado.
    // - Pago aprobado: orden PAGADA, se descuenta stock y se vacia el carrito.
    // - Pago rechazado: orden RECHAZADA, el carrito y el stock quedan intactos (puede reintentar).
    // Al ser @Transactional, las entidades modificadas (stock, carrito) se guardan automaticamente al terminar el metodo; si algo falla, se revierte todo.


    @Transactional
    public OrdenCompra checkout(Long usuarioId, String numeroTarjeta) {
        Carrito carrito = carritoService.obtenerCarritoPorUsuario(usuarioId);

        if (carrito.getItems().isEmpty()) {
            throw new OrdenCompraInvalidaException("El carrito está vacío");
        }

        String tarjeta = normalizarTarjeta(numeroTarjeta);

        OrdenCompra orden = OrdenCompra.builder()
                .usuario(carrito.getUsuario())
                .metodoPago("TARJETA_SIMULADA")
                .tarjetaUltimos4(tarjeta.substring(tarjeta.length() - 4))
                .build();



        // Armar items y calcular el total en el servidor con los precios actuales
        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItem ci : carrito.getItems()) {
            Producto producto = ci.getProducto();

            if (ci.getCantidad() == null || ci.getCantidad() <= 0) {
                throw new OrdenCompraInvalidaException("Cantidad inválida para " + producto.getNombre());
            }
            if (producto.getStock() < ci.getCantidad()) {
                throw new OrdenCompraInvalidaException("Stock insuficiente para " + producto.getNombre());
            }

            orden.getItems().add(OrdenCompraItem.builder()
                    .orden(orden)
                    .producto(producto)
                    .cantidad(ci.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .build());

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(ci.getCantidad())));
        }
        orden.setTotal(total);



        // Pago simulado
        ResultadoPago pago = pagoSimuladoService.procesar(tarjeta, total);
        orden.setTransaccionId(pago.transaccionId());



        // Segun el resultado se aprueba o se rechaza
        if (pago.aprobado()) {
            orden.setEstado(EstadoOrdenCompra.PAGADA);
            for (OrdenCompraItem oi : orden.getItems()) {
                Producto p = oi.getProducto();
                p.setStock(p.getStock() - oi.getCantidad());
                productoRepository.save(p);
            }
            carrito.getItems().clear(); // orphanRemoval borra los CarritoItem
        } else {
            orden.setEstado(EstadoOrdenCompra.RECHAZADA);
            orden.setMotivoRechazo(pago.motivo());
        }

        return ordenRepository.save(orden);
    }


    @Transactional(readOnly = true)
    public List<OrdenCompraResponseDTO> listarPorUsuario(Long usuarioId) {
        return ordenRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public OrdenCompraResponseDTO obtenerPorId(Long ordenId) {
        OrdenCompra orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de compra no encontrada"));
        return convertirADTO(orden);
    }


    // Limpia espacios/guiones y valida que sean solo digitos (minimo 4).
    private String normalizarTarjeta(String numeroTarjeta) {
        if (numeroTarjeta == null) {
            throw new OrdenCompraInvalidaException("Falta el número de tarjeta");
        }
        String limpio = numeroTarjeta.replaceAll("[\\s-]", "");
        if (!limpio.matches("\\d{4,19}")) {
            throw new OrdenCompraInvalidaException("Número de tarjeta inválido");
        }
        return limpio;
    }



    public OrdenCompraResponseDTO convertirADTO(OrdenCompra orden) {
        List<OrdenCompraItemResponseDTO> items = orden.getItems().stream()
                .map(oi -> OrdenCompraItemResponseDTO.builder()
                        .productoId(oi.getProducto().getId())
                        .nombreProducto(oi.getProducto().getNombre())
                        .precioUnitario(oi.getPrecioUnitario())
                        .cantidad(oi.getCantidad())
                        .subtotal(oi.getPrecioUnitario().multiply(BigDecimal.valueOf(oi.getCantidad())))
                        .build())
                .collect(Collectors.toList());

        return OrdenCompraResponseDTO.builder()
                .id(orden.getId())
                .usuarioId(orden.getUsuario().getId())
                .estado(orden.getEstado())
                .total(orden.getTotal())
                .fecha(orden.getFecha())
                .metodoPago(orden.getMetodoPago())
                .tarjetaUltimos4(orden.getTarjetaUltimos4())
                .transaccionId(orden.getTransaccionId())
                .motivoRechazo(orden.getMotivoRechazo())
                .items(items)
                .build();
    }
}