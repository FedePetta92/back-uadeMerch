package com.uade.e_commerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Carrito;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.repository.CarritoRepository;
import com.uade.e_commerce.repository.ProductoRepository;
import com.uade.e_commerce.repository.UsuarioRepository;

@Service 
public class CarritoService {
    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoItemService carritoItemService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /** Busca el carrito del usuario, si todavia no tiene uno lo crea vacio. */
    private Carrito obtenerOcrearCarritoPorUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearCarritoParaUsuario(usuarioId));
    }

    private Carrito crearCarritoParaUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Carrito nuevoCarrito = Carrito.builder()
                .usuario(usuario)
                .items(new ArrayList<>())
                .build();

        return carritoRepository.save(nuevoCarrito);
    }

    public Carrito obtenerCarritoPorUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));
    }

    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        Carrito carrito = obtenerOcrearCarritoPorUsuario(usuarioId);
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
