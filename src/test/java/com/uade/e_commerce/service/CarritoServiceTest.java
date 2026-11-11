package com.uade.e_commerce.service;

import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Carrito;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.model.Usuario;
import com.uade.e_commerce.repository.CarritoRepository;
import com.uade.e_commerce.repository.ProductoRepository;
import com.uade.e_commerce.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CarritoItemService carritoItemService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CarritoService carritoService;

    // nombre de los test -> metodo_escenario_resultadoEsperado

    @Test
    void obtenerCarritoPorUsuario_existente_devuelveCarrito() {
        Carrito carrito = Carrito.builder().id(1L).build();
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        assertThat(carritoService.obtenerCarritoPorUsuario(1L)).isEqualTo(carrito);
    }

    @Test
    void obtenerCarritoPorUsuario_noExistente_lanzaExcepcion() {
        when(carritoRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.obtenerCarritoPorUsuario(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Carrito no encontrado para el usuario 99");
    }

    @Test
    void agregarProducto_sinCarritoPrevio_creaCarritoYAgregaItem() {
        Usuario usuario = Usuario.builder().id(1L).email("test@test.com").build();
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        Carrito carritoVacio = Carrito.builder().id(1L).usuario(usuario).items(new ArrayList<>()).build();

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(usuarioRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        // Primera llamada a save: persiste el carrito vacio. Segunda: lo guarda con el item.
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carritoVacio)
                .thenAnswer(inv -> inv.getArgument(0));
        when(productoRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(producto));

        Carrito resultado = carritoService.agregarProducto(1L, 10L, 2);

        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.getItems().get(0).getProducto()).isEqualTo(producto);
        assertThat(resultado.getItems().get(0).getCantidad()).isEqualTo(2);
        verify(carritoRepository, times(2)).save(any(Carrito.class));
    }

    @Test
    void agregarProducto_conCarritoExistente_agregaItemAlCarritoExistente() {
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        Carrito carritoExistente = Carrito.builder().id(1L).items(new ArrayList<>()).build();

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoExistente));
        when(productoRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito resultado = carritoService.agregarProducto(1L, 10L, 3);

        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.getItems().get(0).getCantidad()).isEqualTo(3);
        // No se crea carrito nuevo: solo una llamada a save
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    void agregarProducto_usuarioInexistente_lanzaExcepcion() {
        when(carritoRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());
        when(usuarioRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.agregarProducto(99L, 10L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregarProducto_productoInactivo_lanzaExcepcion() {
        Carrito carritoExistente = Carrito.builder().id(1L).items(new ArrayList<>()).build();

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoExistente));
        when(productoRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.agregarProducto(1L, 10L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void eliminarProducto_conItemExistente_loQuita() {
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        CarritoItem item = CarritoItem.builder().id(5L).producto(producto).cantidad(1).build();
        Carrito carrito = Carrito.builder().id(1L).items(new ArrayList<>(List.of(item))).build();

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        Carrito resultado = carritoService.eliminarProducto(1L, 5L);

        assertThat(resultado.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }

    @Test
    void calcularTotal_conDosItems_sumaSubtotales() {
        Producto p1 = Producto.builder().id(1L).nombre("Remera").precio(BigDecimal.valueOf(500)).stock(5).build();
        Producto p2 = Producto.builder().id(2L).nombre("Buzo").precio(BigDecimal.valueOf(1000)).stock(3).build();
        CarritoItem item1 = CarritoItem.builder().id(1L).producto(p1).cantidad(2).build();
        CarritoItem item2 = CarritoItem.builder().id(2L).producto(p2).cantidad(1).build();
        Carrito carrito = Carrito.builder().id(1L).items(List.of(item1, item2)).build();

        when(carritoItemService.calcularSubtotal(item1)).thenReturn(BigDecimal.valueOf(1000)); // 500 * 2
        when(carritoItemService.calcularSubtotal(item2)).thenReturn(BigDecimal.valueOf(1000)); // 1000 * 1

        BigDecimal total = carritoService.calcularTotal(carrito);

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    void calcularTotal_carritoVacio_devuelveCero() {
        Carrito carrito = Carrito.builder().id(1L).items(new ArrayList<>()).build();

        BigDecimal total = carritoService.calcularTotal(carrito);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }
}