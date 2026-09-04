package com.uade.e_commerce.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Carrito;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.CarritoRepository;
import com.uade.e_commerce.repository.ProductoRepository;

@Service 
public class CarritoService {
    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoItemService carritoItemService;

    public Carrito obtenerCarritoPorUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));
    }

    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        Carrito carrito = obtenerCarritoPorUsuario(usuarioId);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        CarritoItem item = new CarritoItem();
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(cantidad);

        carrito.getItems().add(item);
        return carritoRepository.save(carrito);
    }

    public Carrito eliminarProducto(Long usuarioId, Long itemId) {
        Carrito carrito = obtenerCarritoPorUsuario(usuarioId);
        carrito.getItems().removeIf(item -> item.getId().equals(itemId));
        return carritoRepository.save(carrito);
    }

    public BigDecimal calcularTotal(Carrito carrito) {
    return carrito.getItems().stream()
            .map(carritoItemService::calcularSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal obtenerTotal(Long usuarioId) {
        Carrito carrito = obtenerCarritoPorUsuario(usuarioId);
        return calcularTotal(carrito);
    }
}
