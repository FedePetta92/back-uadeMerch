package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.OrdenCompraResponseDTO;
import com.uade.e_commerce.exceptions.OrdenCompraInvalidaException;
import com.uade.e_commerce.model.*;
import com.uade.e_commerce.repository.OrdenCompraRepository;
import com.uade.e_commerce.repository.ProductoRepository;
import com.uade.e_commerce.service.PagoSimuladoService.ResultadoPago;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenCompraServiceTest {

    @Mock
    private OrdenCompraRepository ordenRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CarritoService carritoService;

    @Mock
    private PagoSimuladoService pagoSimuladoService;

    @InjectMocks
    private OrdenCompraService ordenCompraService;

    // nombre de los test -> metodo_escenario_resultadoEsperado

    @Test
    void checkout_pagoAprobado_creaOrdenPagadaYDescontaStock() {
        Usuario usuario = Usuario.builder().id(1L).email("test@test.com").build();
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.valueOf(500)).stock(10).build();
        CarritoItem item = CarritoItem.builder().producto(producto).cantidad(2).build();
        Carrito carrito = Carrito.builder().usuario(usuario).items(new ArrayList<>(List.of(item))).build();

        when(carritoService.obtenerCarritoPorUsuario(1L)).thenReturn(carrito);
        when(pagoSimuladoService.procesar(any(), any())).thenReturn(new ResultadoPago(true, "SIM-ABC123", null));
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ordenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra orden = ordenCompraService.checkout(1L, "4111111111111111");

        assertThat(orden.getEstado()).isEqualTo(EstadoOrdenCompra.PAGADA);
        assertThat(orden.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(1000)); // 500 * 2
        assertThat(orden.getTarjetaUltimos4()).isEqualTo("1111");
        assertThat(producto.getStock()).isEqualTo(8); // 10 - 2: se desconto el stock
        assertThat(carrito.getItems()).isEmpty(); // el carrito quedo vacio
    }

    @Test
    void checkout_pagoRechazado_creaOrdenRechazadaSinModificarStockNiCarrito() {
        Usuario usuario = Usuario.builder().id(1L).email("test@test.com").build();
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.valueOf(500)).stock(10).build();
        CarritoItem item = CarritoItem.builder().producto(producto).cantidad(2).build();
        Carrito carrito = Carrito.builder().usuario(usuario).items(new ArrayList<>(List.of(item))).build();

        when(carritoService.obtenerCarritoPorUsuario(1L)).thenReturn(carrito);
        when(pagoSimuladoService.procesar(any(), any())).thenReturn(new ResultadoPago(false, "SIM-XYZ789", "TARJETA_RECHAZADA"));
        when(ordenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra orden = ordenCompraService.checkout(1L, "1234567890000002");

        assertThat(orden.getEstado()).isEqualTo(EstadoOrdenCompra.RECHAZADA);
        assertThat(orden.getMotivoRechazo()).isEqualTo("TARJETA_RECHAZADA");
        assertThat(producto.getStock()).isEqualTo(10); // stock sin cambios
        assertThat(carrito.getItems()).hasSize(1); // carrito sin cambios: puede reintentar
        verify(productoRepository, never()).save(any());
    }

    @Test
    void checkout_carritoVacio_lanzaExcepcion() {
        Carrito carrito = Carrito.builder()
                .usuario(Usuario.builder().id(1L).build())
                .items(new ArrayList<>())
                .build();
        when(carritoService.obtenerCarritoPorUsuario(1L)).thenReturn(carrito);

        assertThatThrownBy(() -> ordenCompraService.checkout(1L, "4111111111111111"))
                .isInstanceOf(OrdenCompraInvalidaException.class)
                .hasMessage("El carrito está vacío");

        verify(ordenRepository, never()).save(any());
    }

    @Test
    void checkout_productoInactivo_lanzaExcepcion() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Producto productoInactivo = Producto.builder()
                .id(10L).nombre("Remera").precio(BigDecimal.TEN).stock(10).activo(false).build();
        CarritoItem item = CarritoItem.builder().producto(productoInactivo).cantidad(1).build();
        Carrito carrito = Carrito.builder().usuario(usuario).items(new ArrayList<>(List.of(item))).build();

        when(carritoService.obtenerCarritoPorUsuario(1L)).thenReturn(carrito);

        assertThatThrownBy(() -> ordenCompraService.checkout(1L, "4111111111111111"))
                .isInstanceOf(OrdenCompraInvalidaException.class)
                .hasMessageContaining("ya no está disponible");

        verify(ordenRepository, never()).save(any());
    }

    @Test
    void checkout_stockInsuficiente_lanzaExcepcion() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.TEN).stock(1).build();
        CarritoItem item = CarritoItem.builder().producto(producto).cantidad(5).build(); // pide 5, solo hay 1
        Carrito carrito = Carrito.builder().usuario(usuario).items(new ArrayList<>(List.of(item))).build();

        when(carritoService.obtenerCarritoPorUsuario(1L)).thenReturn(carrito);

        assertThatThrownBy(() -> ordenCompraService.checkout(1L, "4111111111111111"))
                .isInstanceOf(OrdenCompraInvalidaException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(ordenRepository, never()).save(any());
    }

    @Test
    void checkout_tarjetaNula_lanzaExcepcion() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.TEN).stock(5).build();
        CarritoItem item = CarritoItem.builder().producto(producto).cantidad(1).build();
        Carrito carrito = Carrito.builder().usuario(usuario).items(new ArrayList<>(List.of(item))).build();

        when(carritoService.obtenerCarritoPorUsuario(1L)).thenReturn(carrito);

        assertThatThrownBy(() -> ordenCompraService.checkout(1L, null))
                .isInstanceOf(OrdenCompraInvalidaException.class)
                .hasMessage("Falta el número de tarjeta");
    }

    @Test
    void checkout_tarjetaConEspaciosYGuiones_funcionaIgual() {
        // "4111-1111-1111-1111" debe normalizarse a "4111111111111111" antes de procesar
        Usuario usuario = Usuario.builder().id(1L).email("test@test.com").build();
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.valueOf(500)).stock(10).build();
        CarritoItem item = CarritoItem.builder().producto(producto).cantidad(1).build();
        Carrito carrito = Carrito.builder().usuario(usuario).items(new ArrayList<>(List.of(item))).build();

        when(carritoService.obtenerCarritoPorUsuario(1L)).thenReturn(carrito);
        when(pagoSimuladoService.procesar(any(), any())).thenReturn(new ResultadoPago(true, "SIM-123", null));
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ordenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra orden = ordenCompraService.checkout(1L, "4111-1111-1111-1111");

        assertThat(orden.getEstado()).isEqualTo(EstadoOrdenCompra.PAGADA);
        assertThat(orden.getTarjetaUltimos4()).isEqualTo("1111"); // confirma que se normalizo correctamente
    }

    @Test
    void listarPorUsuario_conOrdenes_devuelveDTOsEnOrden() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Producto producto = Producto.builder().id(10L).nombre("Remera").precio(BigDecimal.valueOf(500)).stock(10).build();
        OrdenCompraItem oi = OrdenCompraItem.builder()
                .producto(producto).cantidad(1).precioUnitario(BigDecimal.valueOf(500)).build();
        OrdenCompra orden = OrdenCompra.builder()
                .id(1L)
                .usuario(usuario)
                .estado(EstadoOrdenCompra.PAGADA)
                .total(BigDecimal.valueOf(500))
                .metodoPago("TARJETA_SIMULADA")
                .tarjetaUltimos4("1111")
                .transaccionId("SIM-ABC")
                .items(new ArrayList<>(List.of(oi)))
                .build();

        when(ordenRepository.findByUsuarioIdOrderByFechaDesc(1L)).thenReturn(List.of(orden));

        List<OrdenCompraResponseDTO> resultado = ordenCompraService.listarPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstado()).isEqualTo(EstadoOrdenCompra.PAGADA);
        assertThat(resultado.get(0).getUsuarioId()).isEqualTo(1L);
        assertThat(resultado.get(0).getTotal()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(resultado.get(0).getItems()).hasSize(1);
        assertThat(resultado.get(0).getItems().get(0).getNombreProducto()).isEqualTo("Remera");
    }
}