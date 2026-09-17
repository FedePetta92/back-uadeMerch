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

/** Pruebas unitarias del flujo de alta y validacion de productos. */
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;


    //nombre de los test -> metodo_escenario_resultadoEsperado

    // Verifica el comportamiento esperado del componente getProductoById que si exista y lo devuelva.

    @Test
    void getProductoById_existente_devuelveElProducto() {
        Producto producto = Producto.builder().id(1L).nombre("Remera").descripcion("desc").precio(BigDecimal.TEN).stock(5).build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        assertThat(productoService.getProductoById(1L)).isEqualTo(producto);
    }

    // Verifica el comportamiento esperado del componente getProducto que no exista.

    @Test
    void getProductoById_noExistente_lanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.getProductoById(1L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado");
    }

    // Verifica el comportamiento esperado del componente getAllProductos y que devuelva lista con productos.

    @Test
    void getAllProductos_conProductos_devuelveLaLista() {
        Producto producto1 = Producto.builder().id(1L).nombre("Remera").descripcion("desc").precio(BigDecimal.TEN).stock(5).build();
        Producto producto2 = Producto.builder().id(2L).nombre("Buzo").descripcion("desc").precio(BigDecimal.valueOf(20)).stock(3).build();
        List<Producto> productos = List.of(producto1, producto2);

        when(productoRepository.findAll()).thenReturn(productos);
        assertThat(productoService.getAllProductos()).isEqualTo(productos);
    }

    // Verifica el comportamiento esperado del componente getAllProductos y que devuelva lista vacia.

    @Test
    void getAllProductos_sinProductos_devuelveListaVacia() {
       List<Producto> productos = List.of();

       when(productoRepository.findAll()).thenReturn(productos);
       assertThat(productoService.getAllProductos()).isEmpty();
    }

    // Verifica el comportamiento esperado del componente deleteProducto.

    @Test
    void deleteProducto_idValido_llamaRepositoryDeleteById() {
        productoService.deleteProducto(1L);

        verify(productoRepository).deleteById(1L);
    }
}
