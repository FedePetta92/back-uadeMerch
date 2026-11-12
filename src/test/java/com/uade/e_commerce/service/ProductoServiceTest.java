package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.ProductoRequestDTO;
import com.uade.e_commerce.dto.ProductoResponseDTO;
import com.uade.e_commerce.dto.ProductoUpdateDTO;
import com.uade.e_commerce.exceptions.PrecioNegativoException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.CarritoItemRepository;
import com.uade.e_commerce.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CarritoItemRepository carritoItemRepository;

    @InjectMocks
    private ProductoService productoService;


    //nombre de los test -> metodo_escenario_resultadoEsperado

    @Test
    void getProductoById_existente_devuelveElProducto() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").descripcion("desc").precio(BigDecimal.TEN).stock(5).build();

        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto));
        assertThat(productoService.getProductoById(1L)).isEqualTo(producto);
    }

    @Test
    void getProductoById_noExistente_lanzaExcepcion() {
        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.getProductoById(1L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getAllProductos_conProductos_devuelveLaLista() {
        Producto producto1 = Producto.builder().id(1L).nombre("Remera").descripcion("desc").precio(BigDecimal.TEN).stock(5).build();
        Producto producto2 = Producto.builder().id(2L).nombre("Buzo").descripcion("desc").precio(BigDecimal.valueOf(20)).stock(3).build();
        List<Producto> productos = List.of(producto1, producto2);

        when(productoRepository.findByActivoTrue()).thenReturn(productos);
        assertThat(productoService.getAllProductos()).isEqualTo(productos);
    }

    @Test
    void getAllProductos_sinProductos_devuelveListaVacia() {
       List<Producto> productos = List.of();

       when(productoRepository.findByActivoTrue()).thenReturn(productos);
       assertThat(productoService.getAllProductos()).isEmpty();
    }

    @Test
    void deleteProducto_existente_loQuitaDeLosCarritosYLoDaDeBaja() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();

        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto));

        productoService.deleteProducto(1L);

        verify(carritoItemRepository).deleteByProductoId(1L);
        assertThat(producto.isActivo()).isFalse();
        assertThat(producto.getFechaBaja()).isNotNull();
        verify(productoRepository).save(producto);
        // Borrado lógico: nunca se elimina la fila
        verify(productoRepository, never()).deleteById(any());
        verify(productoRepository, never()).delete(any());
    }

    @Test
    void deleteProducto_noExistenteODadoDeBaja_lanzaExcepcion() {
        when(productoRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.deleteProducto(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado");

        verify(carritoItemRepository, never()).deleteByProductoId(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void productoNuevo_conBuilder_quedaActivo() {
        Producto producto = Producto.builder().nombre("Remera").build();

        assertThat(producto.isActivo()).isTrue();
    }

    @Test
    void saveProducto_precioPositivo_guardaYDevuelveDTO() {
        ProductoRequestDTO request = ProductoRequestDTO.builder()
                .nombre("Remera")
                .descripcion("desc")
                .precio(BigDecimal.TEN)
                .stock(5)
                .build();

        Producto productoGuardado = Producto.builder()
                .id(1L).nombre("Remera").descripcion("desc").precio(BigDecimal.TEN).stock(5).build();

        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        ProductoResponseDTO response = productoService.saveProducto(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Remera");
        assertThat(response.getPrecio()).isEqualByComparingTo(BigDecimal.TEN);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void saveProducto_precioNegativo_lanzaExcepcion() {
        ProductoRequestDTO request = ProductoRequestDTO.builder()
                .nombre("Remera")
                .precio(BigDecimal.valueOf(-1))
                .stock(5)
                .build();

        assertThatThrownBy(() -> productoService.saveProducto(request))
                .isInstanceOf(PrecioNegativoException.class)
                .hasMessage("El precio no puede ser negativo");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void updateProducto_existente_actualizaDatosYDevuelveDTO() {
        ProductoUpdateDTO dto = new ProductoUpdateDTO();
        dto.setNombre("Buzo");
        dto.setPrecio(BigDecimal.valueOf(500));
        dto.setStock(3);

        Producto productoExistente = Producto.builder()
                .id(1L).nombre("Remera").descripcion("desc").precio(BigDecimal.TEN).stock(5).build();
        Producto productoActualizado = Producto.builder()
                .id(1L).nombre("Buzo").descripcion("desc").precio(BigDecimal.valueOf(500)).stock(3).build();

        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(productoExistente)).thenReturn(productoActualizado);

        ProductoResponseDTO response = productoService.updateProducto(1L, dto);

        assertThat(response.getNombre()).isEqualTo("Buzo");
        assertThat(response.getPrecio()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(response.getStock()).isEqualTo(3);
    }

    @Test
    void updateProducto_noExistente_lanzaExcepcion() {
        ProductoUpdateDTO dto = new ProductoUpdateDTO();
        dto.setNombre("Buzo");
        dto.setPrecio(BigDecimal.TEN);
        dto.setStock(5);

        when(productoRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.updateProducto(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void updateProducto_precioNegativo_lanzaExcepcion() {
        ProductoUpdateDTO dto = new ProductoUpdateDTO();
        dto.setNombre("Buzo");
        dto.setPrecio(BigDecimal.valueOf(-10));
        dto.setStock(5);

        assertThatThrownBy(() -> productoService.updateProducto(1L, dto))
                .isInstanceOf(PrecioNegativoException.class)
                .hasMessage("El precio no puede ser negativo");
    }
}
