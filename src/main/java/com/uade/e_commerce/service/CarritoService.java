package com.uade.e_commerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.exceptions.CantidadInvalidaException;
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

    /**
     * Busca el carrito del usuario, si todavia no tiene uno lo crea vacio.
     * Lo usa GET /api/carritos/{usuarioId}: un usuario recién registrado ve un carrito vacío en vez de un 404.
     */
    public Carrito obtenerOcrearCarritoPorUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearCarritoParaUsuario(usuarioId));
    }

    /** Crea un carrito vacío para un usuario activo. Si no existe o está dado de baja, lanza RecursoNoEncontradoException. */
    private Carrito crearCarritoParaUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findByIdAndActivoTrue(usuarioId)
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

    /**
     * Agrega un producto al carrito del usuario (si el usuario no tiene carrito, lo crea).
     * Solo se pueden agregar productos activos: si no existe o está dado de baja, lanza RecursoNoEncontradoException.
     * Si el producto ya estaba en el carrito, suma la cantidad al item existente en vez de crear otro.
     * Lanza CantidadInvalidaException (400) si la cantidad es menor a 1 o si el total supera el stock.
     */
    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new CantidadInvalidaException("La cantidad tiene que ser al menos 1");
        }

        Carrito carrito = obtenerOcrearCarritoPorUsuario(usuarioId);
        // Un producto dado de baja ya no se puede agregar al carrito
        Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        CarritoItem existente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(productoId))
                .findFirst()
                .orElse(null);

        int cantidadFinal = cantidad + (existente != null ? existente.getCantidad() : 0);
        if (cantidadFinal > producto.getStock()) {
            throw new CantidadInvalidaException("Stock insuficiente para " + producto.getNombre()
                    + ": quedan " + producto.getStock() + " unidades");
        }

        if (existente != null) {
            existente.setCantidad(cantidadFinal);
        } else {
            CarritoItem item = new CarritoItem();
            item.setCarrito(carrito);
            item.setProducto(producto);
            item.setCantidad(cantidad);
            carrito.getItems().add(item);
        }
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
