package com.uade.e_commerce.service;

import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Producto;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;


    //nombre de los test -> metodo_escenario_resultadoEsperado

    @Test
    void getProductoById_existente_devuelveElProducto() {
        Producto producto = new Producto(1L, "Remera", "desc", BigDecimal.TEN, 5, null);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        assertThat(productoService.getProductoById(1L)).isEqualTo(producto);
    }

    @Test
    void getProductoById_noExistente_lanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.getProductoById(1L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getAllProductos_conProductos_devuelveLaLista() {
        Producto producto1 = new Producto(1L, "Remera", "desc", BigDecimal.TEN, 5, null);
        Producto producto2 = new Producto(2L, "Buzo", "desc", BigDecimal.valueOf(20), 3, null);
        List<Producto> productos = List.of(producto1, producto2);

        when(productoRepository.findAll()).thenReturn(productos);
        assertThat(productoService.getAllProductos()).isEqualTo(productos);
    }

    @Test
    void getAllProductos_sinProductos_devuelveListaVacia() {
       List<Producto> productos = List.of();

       when(productoRepository.findAll()).thenReturn(productos);
       assertThat(productoService.getAllProductos()).isEmpty();
    }

    @Test
    void deleteProducto_idValido_llamaRepositoryDeleteById() {
        productoService.deleteProducto(1L);

        verify(productoRepository).deleteById(1L);
    }
}
