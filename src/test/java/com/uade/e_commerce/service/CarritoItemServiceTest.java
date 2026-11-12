package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.CarritoItemResponseDTO;
import com.uade.e_commerce.exceptions.CantidadInvalidaException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.CarritoItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarritoItemServiceTest {

    @Mock
    private CarritoItemRepository carritoItemRepository;

    @InjectMocks
    private CarritoItemService carritoItemService;

    // nombre de los test -> metodo_escenario_resultadoEsperado

    @Test
    void calcularSubtotal_conProductoYCantidad_devuelvePrecioMultiplicado() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").precio(BigDecimal.valueOf(500)).stock(5).build();
        CarritoItem item = CarritoItem.builder().id(1L).producto(producto).cantidad(3).build();

        BigDecimal subtotal = carritoItemService.calcularSubtotal(item);

        assertThat(subtotal).isEqualByComparingTo(BigDecimal.valueOf(1500)); // 500 * 3
    }

    @Test
    void obtenerPorId_existente_devuelveItem() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        CarritoItem item = CarritoItem.builder().id(1L).producto(producto).cantidad(2).build();

        when(carritoItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThat(carritoItemService.obtenerPorId(1L)).isEqualTo(item);
    }

    @Test
    void obtenerPorId_noExistente_lanzaExcepcion() {
        when(carritoItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoItemService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Ítem no encontrado");
    }

    @Test
    void actualizarCantidad_existente_guardaYDevuelveConNuevaCantidad() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        CarritoItem item = CarritoItem.builder().id(1L).producto(producto).cantidad(1).build();
        CarritoItem itemActualizado = CarritoItem.builder().id(1L).producto(producto).cantidad(5).build();

        when(carritoItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(carritoItemRepository.save(item)).thenReturn(itemActualizado);

        CarritoItem resultado = carritoItemService.actualizarCantidad(1L, 5);

        assertThat(resultado.getCantidad()).isEqualTo(5);
        verify(carritoItemRepository).save(item);
    }

    @Test
    void actualizarCantidad_itemInexistente_lanzaExcepcion() {
        when(carritoItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoItemService.actualizarCantidad(99L, 3))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Ítem no encontrado");
    }

    @Test
    void actualizarCantidad_mayorAlStock_lanzaExcepcionYNoGuarda() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        CarritoItem item = CarritoItem.builder().id(1L).producto(producto).cantidad(1).build();
        when(carritoItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> carritoItemService.actualizarCantidad(1L, 6))
                .isInstanceOf(CantidadInvalidaException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(carritoItemRepository, never()).save(any());
    }

    @Test
    void actualizarCantidad_cero_lanzaExcepcionYNoGuarda() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        CarritoItem item = CarritoItem.builder().id(1L).producto(producto).cantidad(1).build();
        when(carritoItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> carritoItemService.actualizarCantidad(1L, 0))
                .isInstanceOf(CantidadInvalidaException.class);
        verify(carritoItemRepository, never()).save(any());
    }

    @Test
    void convertirADTO_mapeoCompletoInclusoSubtotal() {
        Producto producto = Producto.builder().id(10L).nombre("Buzo").precio(BigDecimal.valueOf(800)).stock(3).build();
        CarritoItem item = CarritoItem.builder().id(5L).producto(producto).cantidad(2).build();

        CarritoItemResponseDTO dto = carritoItemService.convertirADTO(item);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getProductoId()).isEqualTo(10L);
        assertThat(dto.getNombreProducto()).isEqualTo("Buzo");
        assertThat(dto.getPrecioUnitario()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertThat(dto.getCantidad()).isEqualTo(2);
        assertThat(dto.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(1600)); // 800 * 2
    }
}